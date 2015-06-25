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
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.IntentSender;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.onepf.opfiab.android.OPFIabActivity;
import org.onepf.opfiab.model.event.android.ActivityNewIntentEvent;
import org.onepf.opfiab.model.event.android.ActivityResultEvent;
import org.onepf.opfiab.model.event.billing.BillingRequest;
import org.onepf.opfiab.sku.SkuResolver;
import org.onepf.opfiab.util.BillingUtils;
import org.onepf.opfiab.util.SyncedReference;
import org.onepf.opfiab.verification.PurchaseVerifier;
import org.onepf.opfutils.OPFLog;

public abstract class ActivityBillingProvider<R extends SkuResolver, V extends PurchaseVerifier>
        extends BaseBillingProvider<R, V> {

    protected static final int DEFAULT_REQUEST_CODE = 4232;


    @Nullable
    private volatile SyncedReference<Activity> syncActivity;
    @Nullable
    private volatile SyncedReference<ActivityResultEvent> syncResult;
    private volatile int pendingRequestCode;

    protected ActivityBillingProvider(@NonNull final Context context,
                                      @NonNull final R skuResolver,
                                      @NonNull final V purchaseVerifier) {
        super(context, skuResolver, purchaseVerifier);
    }

    @CallSuper
    @Override
    protected void handleRequest(@NonNull final BillingRequest billingRequest) {
        super.handleRequest(billingRequest);
        final SyncedReference<Activity> syncActivity = this.syncActivity;
        final Activity activity = syncActivity == null ? null : syncActivity.getNow();
        if (activity instanceof OPFIabActivity) {
            activity.finish();
        }
        this.syncActivity = null;
    }

    @Nullable
    protected final Activity getResultHandlingActivity(
            @NonNull final BillingRequest billingRequest) {
        final Activity activity = BillingUtils.getActivity(billingRequest);
        if (activity != null && billingRequest.isActivityHandlesResult()) {
            return activity;
        }
        final SyncedReference<Activity> syncActivity = this.syncActivity;
        final Activity opfActivity = syncActivity == null ? null : syncActivity.getNow();
        if (opfActivity != null) {
            return opfActivity;
        }
        final SyncedReference<Activity> newSyncActivity = new SyncedReference<>();
        this.syncActivity = newSyncActivity;
        OPFIabActivity.start(activity != null ? activity : context);
        return newSyncActivity.get();
    }

    @Nullable
    protected ActivityResultEvent requestActivityResult(
            @NonNull final BillingRequest billingRequest,
            @NonNull final ActivityForResultLauncher launcher,
            final int requestCode) {
        final Activity activity = getResultHandlingActivity(billingRequest);
        if (activity == null) {
            return null;
        }
        try {
            final SyncedReference<ActivityResultEvent> syncResult = new SyncedReference<>();
            this.syncResult = syncResult;
            this.pendingRequestCode = requestCode;
            launcher.onStartForResult(activity);
            return syncResult.get();
        } catch (ActivityNotFoundException | IntentSender.SendIntentException exception) {
            OPFLog.e("", exception);
            this.syncResult = null;
        }
        return null;
    }

    public final void onEventMainThread(@NonNull final ActivityNewIntentEvent intentEvent) {
        final SyncedReference<Activity> syncActivity = this.syncActivity;
        if (syncActivity != null) {
            final Activity activity = intentEvent.getActivity();
            syncActivity.set(activity);
        }
    }

    @CallSuper
    public void onEventMainThread(@NonNull final ActivityResultEvent event) {
        final SyncedReference<ActivityResultEvent> syncResult = this.syncResult;
        if (syncResult != null && event.getRequestCode() == pendingRequestCode) {
            this.syncResult = null;
            syncResult.set(event);
        }
    }

    protected interface ActivityForResultLauncher {

        void onStartForResult(@NonNull final Activity activity)
                throws IntentSender.SendIntentException;
    }
}
