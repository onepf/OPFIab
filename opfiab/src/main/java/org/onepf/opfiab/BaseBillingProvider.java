/*
 * Copyright 2012-2014 One Platform Foundation
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

package org.onepf.opfiab;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import org.onepf.opfiab.model.BillingProviderInfo;
import org.onepf.opfiab.model.billing.BillingModel;
import org.onepf.opfiab.model.billing.Purchase;
import org.onepf.opfiab.model.billing.SkuDetails;
import org.onepf.opfiab.model.event.ActivityResultEvent;
import org.onepf.opfiab.model.event.BillingEvent;
import org.onepf.opfiab.model.event.request.ConsumeRequest;
import org.onepf.opfiab.model.event.request.PurchaseRequest;
import org.onepf.opfiab.model.event.request.Request;
import org.onepf.opfiab.model.event.request.SkuDetailsRequest;
import org.onepf.opfiab.model.event.response.InventoryResponse;
import org.onepf.opfiab.model.event.response.PurchaseResponse;
import org.onepf.opfiab.model.event.response.Response;
import org.onepf.opfiab.model.event.response.SkuDetailsResponse;
import org.onepf.opfiab.sku.SkuResolver;
import org.onepf.opfiab.verification.PurchaseVerifier;
import org.onepf.opfutils.OPFLog;
import org.onepf.opfutils.OPFPreferences;
import org.onepf.opfutils.OPFUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import de.greenrobot.event.EventBus;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

public abstract class BaseBillingProvider implements BillingProvider {

    private static final String KEY_REQUEST = "request";
    @Nullable
    private static OPFPreferences preferences;
    @Nullable
    private static Request pendingRequest;

    @NonNull
    static OPFPreferences getPreferences(@NonNull final Context context) {
        if (preferences == null) {
            preferences = new OPFPreferences(context);
        }
        return preferences;
    }

    @Nullable
    static Request getPendingRequest(@NonNull final Context context) {
        final OPFPreferences preferences = getPreferences(context);
        if (pendingRequest == null && preferences.contains(KEY_REQUEST)) {
            pendingRequest = BillingEvent.fromJson(preferences.getString(KEY_REQUEST, ""),
                                                   Request.class);
        }
        return pendingRequest;
    }

    static void setPendingRequest(@NonNull final Context context,
                                  @Nullable final Request pendingRequest) {
        final OPFPreferences preferences = getPreferences(context);
        BaseBillingProvider.pendingRequest = pendingRequest;
        if (pendingRequest == null) {
            preferences.remove(KEY_REQUEST);
        } else {
            preferences.put(KEY_REQUEST, pendingRequest.toJson());
        }
    }


    @NonNull
    protected final EventBus eventBus = OPFIab.getEventBus();
    @NonNull
    protected final Context context;
    @NonNull
    protected final PurchaseVerifier purchaseVerifier;
    @NonNull
    protected final SkuResolver skuResolver;

    protected BaseBillingProvider(
            @NonNull final Context context,
            @NonNull final PurchaseVerifier purchaseVerifier,
            @NonNull final SkuResolver skuResolver) {
        this.context = context.getApplicationContext();
        this.purchaseVerifier = purchaseVerifier;
        this.skuResolver = skuResolver;
    }

    public final void onEventAsync(@NonNull final Request event) {
        final Request request = getPendingRequest(context);
        if (request != null) {
            OPFLog.e("Request skipped!\n%s", request);
        }
        setPendingRequest(context, event);
        handleRequest(event);
    }

    public final void onEventAsync(@NonNull final ActivityResultEvent event) {
        final Activity activity = event.getActivity();
        final int requestCode = event.getRequestCode();
        final int resultCode = event.getResultCode();
        final Intent data = event.getData();
        onActivityResult(activity, requestCode, resultCode, data);
    }

    @SuppressFBWarnings({"BC_UNCONFIRMED_CAST", "DLS_DEAD_LOCAL_STORE"})
    @SuppressWarnings("ConstantConditions")
    protected void handleRequest(@NonNull final Request request) {
        final String resolvedSku;
        switch (request.getType()) {
            case CONSUME:
                final ConsumeRequest consumeRequest = (ConsumeRequest) request;
                final Purchase purchase = consumeRequest.getPurchase();
                resolvedSku = skuResolver.resolve(purchase.getSku());
                consume(OPFIabUtils.substituteSku(purchase, resolvedSku));
                break;
            case PURCHASE:
                final PurchaseRequest purchaseRequest = (PurchaseRequest) request;
                final Activity activity = purchaseRequest.getActivity();
                resolvedSku = skuResolver.resolve(purchaseRequest.getSku());
                purchase(activity, resolvedSku);
                break;
            case SKU_DETAILS:
                final SkuDetailsRequest skuDetailsRequest = (SkuDetailsRequest) request;
                final Set<String> skus = skuDetailsRequest.getSkus();
                final Set<String> resolvedSkus = OPFIabUtils.resolveSkus(skuResolver, skus);
                skuDetails(resolvedSkus);
                break;
            case INVENTORY:
                inventory();
                break;
        }
    }

    protected final void postResponse(@NonNull final Response response) {
        setPendingRequest(context, null);
        eventBus.postSticky(response);
    }

    @NonNull
    private Request getPendingRequestOrFail() {
        final Request request = getPendingRequest(context);
        if (request == null) {
            throw new IllegalStateException();
        }
        return request;
    }

    protected void postResponse(@NonNull final Response.Status status) {
        final Request request = getPendingRequestOrFail();
        postResponse(OPFIabUtils.emptyResponse(getInfo(), request, status));
    }

    protected void postResponse(@NonNull final Response.Status status,
                                @NonNull final Purchase purchase) {
        final PurchaseRequest purchaseRequest = (PurchaseRequest) getPendingRequestOrFail();
        final Purchase revertedPurchase = OPFIabUtils.revert(purchase, skuResolver);
        postResponse(new PurchaseResponse(getInfo(), purchaseRequest, status, revertedPurchase));
    }

    @SuppressWarnings("unchecked")
    protected void postResponse(@NonNull final Response.Status status,
                                @NonNull final Collection<? extends BillingModel> items) {
        final BillingProviderInfo info = getInfo();
        final Request request = getPendingRequestOrFail();
        final Response response;
        switch (request.getType()) {
            case SKU_DETAILS:
                final Collection<SkuDetails> skusDetails = (Collection<SkuDetails>) items;
                final List<SkuDetails> revertedSkuDetails = new ArrayList<>(items.size());
                for (final SkuDetails skuDetails : skusDetails) {
                    revertedSkuDetails.add(OPFIabUtils.revert(skuDetails, skuResolver));
                }
                response = new SkuDetailsResponse(info, request, status, revertedSkuDetails);
                break;
            case INVENTORY:
                final Collection<Purchase> inventory = (Collection<Purchase>) items;
                final List<Purchase> revertedInventory = new ArrayList<>(items.size());
                for (final Purchase purchase : inventory) {
                    revertedInventory.add(OPFIabUtils.revert(purchase, skuResolver));
                }
                response = new InventoryResponse(info, request, status, revertedInventory);
                break;
            default:
                throw new IllegalStateException("Response type is unmatched by request.");
        }
        postResponse(response);
    }

    @Override
    public void onActivityResult(@NonNull final Activity activity, final int requestCode,
                                 final int resultCode, @Nullable final Intent data) { }

    @Override
    public boolean isAvailable() {
        final String packageName = getInfo().getPackageName();
        if (TextUtils.isEmpty(packageName)) {
            throw new UnsupportedOperationException(
                    "You must override this method for packageless Billing Providers.");
        }
        return OPFUtils.isInstalled(OPFIab.getContext(), packageName);
    }

    @Nullable
    @Override
    public Intent getStorePageIntent() {
        return null;
    }

    @Nullable
    @Override
    public Intent getRateIntent() {
        return null;
    }

    //CHECKSTYLE:OFF
    @Override
    public boolean equals(final Object o) {
        final BillingProviderInfo info = getInfo();
        if (this == o) return true;
        if (!(o instanceof BaseBillingProvider)) return false;

        final BaseBillingProvider that = (BaseBillingProvider) o;

        //noinspection RedundantIfStatement
        if (!info.equals(that.getInfo())) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return getInfo().hashCode();
    }
    //CHECKSTYLE:ON

    public abstract static class Builder {

        @NonNull
        protected final Context context;
        @NonNull
        protected PurchaseVerifier purchaseVerifier = PurchaseVerifier.STUB;
        @NonNull
        protected SkuResolver skuResolver = SkuResolver.STUB;

        protected Builder(@NonNull final Context context) {
            this.context = context;
        }

        protected Builder setPurchaseVerifier(@NonNull final PurchaseVerifier purchaseVerifier) {
            this.purchaseVerifier = purchaseVerifier;
            return this;
        }

        protected Builder setSkuResolver(@NonNull final SkuResolver skuResolver) {
            this.skuResolver = skuResolver;
            return this;
        }

        public abstract BaseBillingProvider build();
    }
}
