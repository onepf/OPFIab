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
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.onepf.opfiab.OPFIab;
import org.onepf.opfiab.misc.OPFIabActivity;
import org.onepf.opfiab.misc.OPFIabUtils;
import org.onepf.opfiab.model.event.RequestHandledEvent;
import org.onepf.opfiab.model.event.android.ActivityIntentEvent;
import org.onepf.opfiab.model.event.billing.BillingRequest;
import org.onepf.opfiab.model.event.billing.PurchaseRequest;
import org.onepf.opfiab.model.event.billing.Status;
import org.onepf.opfiab.sku.SkuResolver;
import org.onepf.opfiab.verification.PurchaseVerifier;
import org.onepf.opfutils.OPFLog;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public abstract class ActivityBillingProvider<R extends SkuResolver, V extends PurchaseVerifier>
        extends BaseBillingProvider<R, V> {

    private static final int ACTIVITY_TIMEOUT = 1000; // 1 second


    private final Semaphore semaphore = new Semaphore(0);
    @Nullable
    private volatile BillingRequest pendingRequest;
    @Nullable
    private volatile BillingRequest activityRequest;

    protected ActivityBillingProvider(@NonNull final Context context,
                                      @NonNull final R skuResolver,
                                      @NonNull final V purchaseVerifier,
                                      @Nullable final Integer requestCode) {
        super(context, skuResolver, purchaseVerifier, requestCode);
    }

    protected ActivityBillingProvider(@NonNull final Context context,
                                      @NonNull final R skuResolver,
                                      @NonNull final V purchaseVerifier) {
        super(context, skuResolver, purchaseVerifier);
    }

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
        final Bundle bundle = new Bundle();
        OPFIabUtils.putRequest(bundle, billingRequest);
        semaphore.drainPermits();
        pendingRequest = billingRequest;
        activityRequest = null;
        OPFIabActivity.start(purchaseRequest.getActivity(), bundle);
        try {
            // Wait for activity to start
            semaphore.tryAcquire(ACTIVITY_TIMEOUT, TimeUnit.MILLISECONDS);
        } catch (InterruptedException exception) {
            OPFLog.e("", exception);
        }
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

    public void onEventMainThread(@NonNull final ActivityIntentEvent activityIntentEvent) {
        final Intent intent = activityIntentEvent.getIntent();
        final BillingRequest billingRequest = OPFIabUtils.getRequest(intent.getExtras());
        if (billingRequest != null && billingRequest.equals(pendingRequest)) {
            final Activity activity = activityIntentEvent.getActivity();
            activityRequest = OPFIabUtils.withActivity(billingRequest, activity);
            semaphore.release();
        }
    }
}
