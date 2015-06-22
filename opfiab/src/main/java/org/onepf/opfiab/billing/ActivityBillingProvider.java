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
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import org.onepf.opfiab.android.OPFIabActivity;
import org.onepf.opfiab.model.billing.Purchase;
import org.onepf.opfiab.model.event.android.ActivityNewIntentEvent;
import org.onepf.opfiab.model.event.android.ActivityResultEvent;
import org.onepf.opfiab.model.event.billing.BillingEventType;
import org.onepf.opfiab.model.event.billing.BillingRequest;
import org.onepf.opfiab.model.event.billing.Status;
import org.onepf.opfiab.sku.SkuResolver;
import org.onepf.opfiab.util.OPFIabUtils;
import org.onepf.opfiab.util.SyncedReference;
import org.onepf.opfiab.verification.PurchaseVerifier;
import org.onepf.opfutils.OPFLog;
import org.onepf.opfutils.OPFUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

/**
 * Extension of {@link BillingProvider} which guarantees non-null {@link Activity} object for all
 * requests matching {@link #needsActivity(BillingRequest)}.
 * <p>
 * New instance of {@link OPFIabActivity} will be launched if necessary.
 */
public abstract class  ActivityBillingProvider<R extends SkuResolver, V extends PurchaseVerifier>
        extends BaseBillingProvider<R, V> {

    /**
     * Default request code to use with {@link Activity#startActivityForResult(Intent, int)}.
     * <p>
     * Can be overridden with {@link #getRequestCodes()}.
     */
    protected static final int REQUEST_CODE = 13685;
    /**
     * Timeout to give up on waiting for a new activity instance.
     */
    protected static final long ACTIVITY_TIMEOUT = 1000L; // 1 second

    @Nullable
    private volatile SyncedReference<Activity> syncActivity;

    protected ActivityBillingProvider(@NonNull final Context context,
                                      @NonNull final R skuResolver,
                                      @NonNull final V purchaseVerifier) {
        super(context, skuResolver, purchaseVerifier);
    }

    /**
     * Gets request codes that should be handled by this billing provider.
     * <p>
     * Must return non-empty collection.
     *
     * @return Collection of request codes that should be handled by this billing provider. Can't be
     * null.
     * @see #REQUEST_CODE
     * @see Activity#startActivityForResult(Intent, int)
     * @see Activity#onActivityResult(int, int, Intent)
     */
    @NonNull
    protected Collection<Integer> getRequestCodes() {
        return Collections.singletonList(REQUEST_CODE);
    }

    protected boolean needsActivity(@NonNull final BillingRequest billingRequest) {
        return billingRequest.getType() == BillingEventType.PURCHASE;
    }

    protected void releaseActivity(@Nullable final Activity activity) {
        if (activity != null && OPFIabUtils.isActivityFake(activity)) {
            activity.finish();
        }
    }

    @Nullable
    private Activity getActivity() {
        final SyncedReference<Activity> syncedReference = syncActivity;
        return syncedReference == null ? null : syncedReference.get();
    }

    @Override
    protected final void purchase(@NonNull final String sku) {
        purchase(getActivity(), sku);
    }

    @Override
    protected final void consume(@NonNull final Purchase purchase) {
        consume(getActivity(), purchase);
    }

    @Override
    protected final void inventory(final boolean startOver) {
        inventory(getActivity(), startOver);
    }

    @Override
    protected final void skuDetails(@NonNull final Set<String> skus) {
        skuDetails(getActivity(), skus);
    }

    protected abstract void purchase(final Activity activity, @NonNull final String sku);

    protected abstract void consume(final Activity activity, @NonNull final Purchase purchase);

    protected abstract void inventory(final Activity activity, final boolean startOver);

    protected abstract void skuDetails(final Activity activity, @NonNull final Set<String> skus);

    @Override
    protected void handleRequest(@NonNull final BillingRequest billingRequest) {
        final SyncedReference<Activity> syncActivity = new SyncedReference<>();
        try {
            this.syncActivity = syncActivity;
            final Activity activity = billingRequest.getActivity();
            final boolean hasActivity = activity != null && billingRequest.isActivityHandlesResult();
            if (!needsActivity(billingRequest) || hasActivity) {
                syncActivity.set(activity);
                super.handleRequest(billingRequest);
                return;
            }
            // We have to start OPFIabActivity to properly handle this request
            OPFIabActivity.start(activity == null ? context : activity);
            final Activity newActivity = syncActivity.get(ACTIVITY_TIMEOUT);
            if (newActivity == null) {
                OPFLog.e("Failed to make new activity for request.");
                postEmptyResponse(billingRequest, Status.UNKNOWN_ERROR);
            } else {
                super.handleRequest(billingRequest);
            }
        } finally {
            this.syncActivity = null;
        }
    }

    @CallSuper
    public void onEventMainThread(@NonNull final ActivityNewIntentEvent intentEvent) {
        final SyncedReference<Activity> syncedReference = syncActivity;
        if (syncedReference != null) {
            final Activity activity = intentEvent.getActivity();
            syncedReference.set(activity);
        }
    }

    public final void onEventAsync(@NonNull final ActivityResultEvent event) {
        deliverActivityResult(event);
    }

    public final void onEventMainThread(@NonNull final ActivityResultEvent event) {
        deliverActivityResult(event);
    }

    private void deliverActivityResult(@NonNull final ActivityResultEvent event) {
        final int requestCode = event.getRequestCode();
        if (!getRequestCodes().contains(requestCode)) {
            return;
        }
        // This request code should be handled by billing provider
        final int resultCode = event.getResultCode();
        final Activity activity = event.getActivity();
        final Intent data = event.getData();
        if (OPFUtils.isMainThread()) {
            onActivityResultSync(activity, requestCode, resultCode, data);
        } else {
            onActivityResult(activity, requestCode, resultCode, data);
        }
    }

    /**
     * Handles result of activity previously started with {@link #REQUEST_CODE}.
     *
     * @see OPFIabActivity
     */
    protected abstract void onActivityResult(@NonNull final Activity activity,
                                    final int requestCode,
                                    final int resultCode,
                                    @Nullable final Intent data);

    /**
     * Same as {@link #onActivityResult(Activity, int, int, Intent)} but called on <b>main thread</b>.
     */
    @SuppressWarnings("PMD.EmptyMethodInAbstractClassShouldBeAbstract")
    protected void onActivityResultSync(@NonNull final Activity activity,
                                        final int requestCode,
                                        final int resultCode,
                                        @Nullable final Intent data) {
        // ignore by default
    }
}
