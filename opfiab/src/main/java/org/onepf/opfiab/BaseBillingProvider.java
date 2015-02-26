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

import org.onepf.opfiab.billing.BillingProvider;
import org.onepf.opfiab.model.BillingProviderInfo;
import org.onepf.opfiab.model.billing.Purchase;
import org.onepf.opfiab.model.billing.SkuDetails;
import org.onepf.opfiab.model.event.ActivityResultEvent;
import org.onepf.opfiab.model.event.RequestHandledEvent;
import org.onepf.opfiab.model.event.billing.BillingRequest;
import org.onepf.opfiab.model.event.billing.BillingResponse;
import org.onepf.opfiab.model.event.billing.ConsumeRequest;
import org.onepf.opfiab.model.event.billing.ConsumeResponse;
import org.onepf.opfiab.model.event.billing.InventoryRequest;
import org.onepf.opfiab.model.event.billing.InventoryResponse;
import org.onepf.opfiab.model.event.billing.PurchaseRequest;
import org.onepf.opfiab.model.event.billing.PurchaseResponse;
import org.onepf.opfiab.model.event.billing.SkuDetailsRequest;
import org.onepf.opfiab.model.event.billing.SkuDetailsResponse;
import org.onepf.opfiab.sku.SkuResolver;
import org.onepf.opfiab.verification.PurchaseVerifier;
import org.onepf.opfutils.OPFLog;
import org.onepf.opfutils.OPFUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import static org.onepf.opfiab.model.event.billing.BillingResponse.Status.BILLING_UNAVAILABLE;
import static org.onepf.opfiab.model.event.billing.BillingResponse.Status.USER_CANCELED;

public abstract class BaseBillingProvider implements BillingProvider {

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

    protected abstract void skuDetails(@NonNull final Set<String> skus);

    protected abstract void inventory(final boolean startOver);

    protected abstract void purchase(@NonNull final Activity activity, @NonNull final String sku);

    protected abstract void consume(@NonNull final Purchase purchase);

    protected void handleRequest(@NonNull final BillingRequest billingRequest) {
        OPFLog.methodD(billingRequest);

        final String resolvedSku;
        switch (billingRequest.getType()) {
            case CONSUME:
                final ConsumeRequest consumeRequest = (ConsumeRequest) billingRequest;
                final Purchase purchase = consumeRequest.getPurchase();
                resolvedSku = skuResolver.resolve(purchase.getSku());
                consume(OPFIabUtils.substituteSku(purchase, resolvedSku));
                break;
            case PURCHASE:
                final PurchaseRequest purchaseRequest = (PurchaseRequest) billingRequest;
                final Activity activity = purchaseRequest.getActivity();
                if (activity == null || activity.isFinishing()) {
                    postEmptyResponse(billingRequest, USER_CANCELED);
                    break;
                }
                resolvedSku = skuResolver.resolve(purchaseRequest.getSku());
                purchase(activity, resolvedSku);
                break;
            case SKU_DETAILS:
                final SkuDetailsRequest skuDetailsRequest = (SkuDetailsRequest) billingRequest;
                final Set<String> skus = skuDetailsRequest.getSkus();
                final Set<String> resolvedSkus = OPFIabUtils.resolveSkus(skuResolver, skus);
                skuDetails(resolvedSkus);
                break;
            case INVENTORY:
                final InventoryRequest inventoryRequest = (InventoryRequest) billingRequest;
                final boolean startOver = inventoryRequest.startOver();
                inventory(startOver);
                break;
        }
    }

    protected void onActivityResult(@NonNull final Activity activity, final int requestCode,
                                    final int resultCode, @Nullable final Intent data) { }

    protected void postResponse(@NonNull final BillingResponse billingResponse) {
        OPFIab.post(billingResponse);
    }

    protected void postEmptyResponse(@NonNull final BillingRequest billingRequest,
                                     @NonNull final BillingResponse.Status status) {
        postResponse(OPFIabUtils.emptyResponse(getInfo(), billingRequest, status));
    }

    protected void postSkuDetailsResponse(@NonNull final BillingResponse.Status status,
                                          @Nullable final Collection<SkuDetails> skusDetails) {
        final SkuDetailsResponse response;
        if (skusDetails == null) {
            response = new SkuDetailsResponse(getInfo(), status, null);
        } else {
            final List<SkuDetails> revertedSkusDetails = new ArrayList<>(skusDetails.size());
            for (final SkuDetails skuDetails : skusDetails) {
                revertedSkusDetails.add(OPFIabUtils.revert(skuResolver, skuDetails));
            }
            response = new SkuDetailsResponse(getInfo(), status, revertedSkusDetails);
        }
        postResponse(response);
    }

    protected void postInventoryResponse(@NonNull final BillingResponse.Status status,
                                         @Nullable final Collection<Purchase> inventory,
                                         final boolean hasMore) {
        final InventoryResponse response;
        if (inventory == null) {
            response = new InventoryResponse(getInfo(), status, null, hasMore);
        } else {
            final List<Purchase> revertedInventory = new ArrayList<>(inventory.size());
            for (final Purchase purchase : inventory) {
                revertedInventory.add(OPFIabUtils.revert(skuResolver, purchase));
            }
            response = new InventoryResponse(getInfo(), status, revertedInventory, hasMore);
        }
        postResponse(response);
    }

    protected void postPurchaseResponse(@NonNull final BillingResponse.Status status,
                                        @Nullable final Purchase purchase) {
        final PurchaseResponse response;
        if (purchase == null) {
            response = new PurchaseResponse(getInfo(), status, null);
        } else {
            final Purchase revertedPurchase = OPFIabUtils.revert(skuResolver, purchase);
            response = new PurchaseResponse(getInfo(), status, revertedPurchase);
        }
        postResponse(response);
    }

    protected void postConsumeResponse(@NonNull final BillingResponse.Status status,
                                       @NonNull final Purchase purchase) {
        final Purchase revertedPurchase = OPFIabUtils.revert(skuResolver, purchase);
        postResponse(new ConsumeResponse(getInfo(), status, revertedPurchase));
    }

    @Override
    public final void onEventAsync(@NonNull final BillingRequest billingRequest) {
        if (!isAvailable()) {
            postEmptyResponse(billingRequest, BILLING_UNAVAILABLE);
        } else {
            handleRequest(billingRequest);
        }
        OPFIab.post(new RequestHandledEvent(billingRequest));
    }

    @Override
    public final void onEventAsync(@NonNull final ActivityResultEvent event) {
        final Activity activity = event.getActivity();
        final int requestCode = event.getRequestCode();
        final int resultCode = event.getResultCode();
        final Intent data = event.getData();
        onActivityResult(activity, requestCode, resultCode, data);
    }

    @Override
    public boolean isAvailable() {
        final String packageName = getInfo().getPackageName();
        if (TextUtils.isEmpty(packageName)) {
            throw new UnsupportedOperationException(
                    "You must override this method for packageless Billing Providers.");
        }
        return OPFUtils.isInstalled(OPFIab.getContext(), packageName);
    }

    @Override
    public boolean isAuthorised() {
        return true;
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

    @Override
    public String toString() {
        return getInfo().toString();
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
