/*
 * Copyright 2012-2015 One Platform Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.onepf.opfiab.billing;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.onepf.opfiab.OPFIab;
import org.onepf.opfiab.android.OPFIabActivity;
import org.onepf.opfiab.model.event.RequestHandledEvent;
import org.onepf.opfiab.model.event.android.ActivityNewIntentEvent;
import org.onepf.opfiab.model.event.android.ActivityResultEvent;
import org.onepf.opfiab.model.event.billing.BillingRequest;
import org.onepf.opfiab.model.event.billing.PurchaseRequest;
import org.onepf.opfiab.model.event.billing.Status;
import org.onepf.opfiab.sku.SkuResolver;
import org.onepf.opfiab.util.OPFIabUtils;
import org.onepf.opfiab.verification.PurchaseVerifier;
import org.onepf.opfutils.OPFLog;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * Extension of {@link BillingProvider} which guarantees non-null {@link Activity} object in {@link #purchase(Activity, String)}.
 * <br>
 * New instance of {@link OPFIabActivity} will be launched if necessary.
 */
public abstract class ActivityBillingProvider<R extends SkuResolver, V extends PurchaseVerifier>
        extends BaseBillingProvider<R, V> {

    private static final long ACTIVITY_TIMEOUT = 1000L; // 1 second


    private final Semaphore semaphore = new Semaphore(0);
    @Nullable
    private volatile BillingRequest pendingRequest;
    @Nullable
    private volatile BillingRequest activityRequest;

    protected ActivityBillingProvider(@NonNull final Context context,
                                      @NonNull final R skuResolver,
                                      @NonNull final V purchaseVerifier) {
        super(context, skuResolver, purchaseVerifier);
    }

    protected abstract void onActivityResult(@NonNull final Activity activity,
                                             final int requestCode,
                                             final int resultCode,
                                             @NonNull final Intent data);

    /**
     * @param activity can't be null
     */
    @Override
    protected abstract void purchase(
            @SuppressWarnings("NullableProblems") @NonNull final Activity activity,
            @NonNull final String sku);

    @Override
    public void onEventAsync(@NonNull final BillingRequest billingRequest) {
        final PurchaseRequest purchaseRequest;
        if (billingRequest.getType() != BillingRequest.Type.PURCHASE
                || !(purchaseRequest = (PurchaseRequest) billingRequest).needsFakeActivity()) {
            super.onEventAsync(billingRequest);
            return;
        }
        // We have to start OPFIabActivity to properly handle this request
        semaphore.drainPermits();
        pendingRequest = billingRequest;
        activityRequest = null;
        final Activity activity = purchaseRequest.getActivity();
        OPFIabActivity.start(activity == null ? context : activity);
        try {
            // Wait for activity to start
            if (!semaphore.tryAcquire(ACTIVITY_TIMEOUT, TimeUnit.MILLISECONDS)) {
                OPFLog.e("Fake activity start time out. Request: %s", billingRequest);
            }
        } catch (InterruptedException exception) {
            OPFLog.e("", exception);
        }
        pendingRequest = null;
        final BillingRequest activityRequest = this.activityRequest;
        if (activityRequest == null) {
            // Can't process request without activity
            OPFLog.e("Failed to add activity to request: %s", billingRequest);
            OPFIab.post(new RequestHandledEvent(billingRequest));
            postEmptyResponse(billingRequest, Status.UNKNOWN_ERROR);
        } else {
            super.onEventAsync(activityRequest);
        }
    }

    public void onEventMainThread(@NonNull final ActivityNewIntentEvent intentEvent) {
        final BillingRequest pendingRequest = this.pendingRequest;
        if (pendingRequest != null) {
            final Activity activity = intentEvent.getActivity();
            activityRequest = OPFIabUtils.withActivity(pendingRequest, activity);
            semaphore.release();
        }
    }

    public final void onEventAsync(@NonNull final ActivityResultEvent event) {
        final int requestCode = event.getRequestCode();
        final Intent data;
        if (requestCode == REQUEST_CODE && (data = event.getData()) != null) {
            final int resultCode = event.getResultCode();
            final Activity activity = event.getActivity();
            onActivityResult(activity, requestCode, resultCode, data);
        }
    }
}
