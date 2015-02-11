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
import org.onepf.opfiab.model.event.billing.ConsumeRequest;
import org.onepf.opfiab.model.event.billing.InventoryResponse;
import org.onepf.opfiab.model.event.billing.PurchaseRequest;
import org.onepf.opfiab.model.event.billing.PurchaseResponse;
import org.onepf.opfiab.model.event.billing.Request;
import org.onepf.opfiab.model.event.billing.Response;
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

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import static org.onepf.opfiab.model.event.billing.Response.Status.BUSY;

public abstract class BaseBillingProvider implements BillingProvider {

    @NonNull
    protected final Context context;
    @NonNull
    protected final PurchaseVerifier purchaseVerifier;
    @NonNull
    protected final SkuResolver skuResolver;

    @Nullable
    private Request pendingRequest;

    protected BaseBillingProvider(
            @NonNull final Context context,
            @NonNull final PurchaseVerifier purchaseVerifier,
            @NonNull final SkuResolver skuResolver) {
        this.context = context.getApplicationContext();
        this.purchaseVerifier = purchaseVerifier;
        this.skuResolver = skuResolver;
    }

    public final void onEventAsync(@NonNull final Request request) {
        if (pendingRequest != null) {
            postResponse(BUSY);
            return;
        }
        pendingRequest = request;
        handleRequest(request);
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

    protected void post(@NonNull final Object event) {
        OPFIab.post(event);
    }

    protected final void postResponse(@NonNull final Response response) {
        pendingRequest = null;
        OPFIab.post(response);
    }

    protected void postResponse(@NonNull final Response.Status status) {
        if (pendingRequest == null) {
            OPFLog.e("No pending request for response! Skipping...\nStatus:%s", status);
            return;
        }
        postResponse(OPFIabUtils.emptyResponse(getInfo(), pendingRequest, status));
    }

    protected void postResponse(@NonNull final Response.Status status,
                                @NonNull final Purchase purchase) {
        if (pendingRequest == null) {
            OPFLog.e("No pending request for response! Skipping...\nStatus:%s\nPurchase:%s",
                     status, purchase);
            return;
        }
        final Purchase revertedPurchase = OPFIabUtils.revert(purchase, skuResolver);
        postResponse(new PurchaseResponse(getInfo(), pendingRequest, status, revertedPurchase));
    }

    @SuppressWarnings("unchecked")
    protected void postResponse(@NonNull final Response.Status status,
                                @NonNull final Collection<? extends BillingModel> items) {
        if (pendingRequest == null) {
            OPFLog.e("No pending request for response! Skipping...\nStatus:%s\nItems:%s",
                     status, items);
            return;
        }
        final BillingProviderInfo info = getInfo();
        final Response response;
        switch (pendingRequest.getType()) {
            case SKU_DETAILS:
                final Collection<SkuDetails> skusDetails = (Collection<SkuDetails>) items;
                final List<SkuDetails> revertedSkuDetails = new ArrayList<>(items.size());
                for (final SkuDetails skuDetails : skusDetails) {
                    revertedSkuDetails.add(OPFIabUtils.revert(skuDetails, skuResolver));
                }
                response = new SkuDetailsResponse(info, pendingRequest, status, revertedSkuDetails);
                break;
            case INVENTORY:
                final Collection<Purchase> inventory = (Collection<Purchase>) items;
                final List<Purchase> revertedInventory = new ArrayList<>(items.size());
                for (final Purchase purchase : inventory) {
                    revertedInventory.add(OPFIabUtils.revert(purchase, skuResolver));
                }
                response = new InventoryResponse(info, pendingRequest, status, revertedInventory);
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
