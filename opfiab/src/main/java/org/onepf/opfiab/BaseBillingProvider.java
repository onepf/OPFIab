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
import org.onepf.opfiab.model.event.billing.Status;
import org.onepf.opfiab.sku.SkuResolver;
import org.onepf.opfiab.verification.PurchaseVerifier;
import org.onepf.opfiab.verification.VerificationResult;
import org.onepf.opfutils.OPFLog;
import org.onepf.opfutils.OPFUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import static org.onepf.opfiab.model.event.billing.Status.BILLING_UNAVAILABLE;
import static org.onepf.opfiab.model.event.billing.Status.USER_CANCELED;

public abstract class BaseBillingProvider<RESOLVER extends SkuResolver, VERIFIER extends PurchaseVerifier>
        implements BillingProvider {

    /**
     * <a href="http://imgs.xkcd.com/comics/random_number.png">http://imgs.xkcd.com/comics/random_number.png</a>
     */
    private static final int DEFAULT_REQUEST_CODE = 13685093;

    @NonNull
    protected final Context context;
    @NonNull
    protected final RESOLVER skuResolver;
    @NonNull
    protected final VERIFIER purchaseVerifier;
    protected final int requestCode;

    protected BaseBillingProvider(@NonNull final Context context,
                                  @NonNull final RESOLVER skuResolver,
                                  @NonNull final VERIFIER purchaseVerifier,
                                  @Nullable final Integer requestCode) {
        checkRequirements();
        this.context = context.getApplicationContext();
        this.purchaseVerifier = purchaseVerifier;
        this.skuResolver = skuResolver;
        this.requestCode = requestCode != null ? requestCode : DEFAULT_REQUEST_CODE;
    }

    protected BaseBillingProvider(@NonNull final Context context,
                                  @NonNull final RESOLVER skuResolver,
                                  @NonNull final VERIFIER purchaseVerifier) {
        this(context, skuResolver, purchaseVerifier, null);
    }

    protected abstract void skuDetails(@NonNull final Set<String> skus);

    protected abstract void inventory(final boolean startOver);

    protected abstract void purchase(@NonNull final Activity activity, @NonNull final String sku);

    protected abstract void consume(@NonNull final Purchase purchase);

    protected void checkRequirements(){
        // Nothing to check by default
    }

    @SuppressFBWarnings({"BC_UNCONFIRMED_CAST"})
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
                final boolean activityFake = purchaseRequest.isActivityFake();
                if (activity == null || !activityFake && !ActivityMonitor.isResumed(activity)) {
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
            default:
                throw new IllegalStateException();
        }
    }

    @SuppressFBWarnings({"ACEM_ABSTRACT_CLASS_EMPTY_METHODS"})
    @SuppressWarnings("PMD.EmptyMethodInAbstractClassShouldBeAbstract")
    protected void onActivityResult(@NonNull final Activity activity, final int requestCode,
                                    final int resultCode, @Nullable final Intent data) {
        // Ignore by default
    }

    protected void postResponse(@NonNull final BillingResponse billingResponse) {
        OPFIab.post(billingResponse);
    }

    protected void postEmptyResponse(@NonNull final BillingRequest billingRequest,
                                     @NonNull final Status status) {
        postResponse(OPFIabUtils.emptyResponse(getInfo(), billingRequest, status));
    }

    protected void postSkuDetailsResponse(@NonNull final Status status,
                                          @Nullable final Collection<SkuDetails> skusDetails) {
        final SkuDetailsResponse response;
        if (skusDetails == null) {
            response = new SkuDetailsResponse(status, getInfo(), null);
        } else {
            final Collection<SkuDetails> revertedSkusDetails = new ArrayList<>(skusDetails.size());
            for (final SkuDetails skuDetails : skusDetails) {
                revertedSkusDetails.add(OPFIabUtils.revert(skuResolver, skuDetails));
            }
            response = new SkuDetailsResponse(status, getInfo(), revertedSkusDetails);
        }
        postResponse(response);
    }

    protected void postInventoryResponse(@NonNull final Status status,
                                         @Nullable final Iterable<Purchase> inventory,
                                         final boolean hasMore) {
        final InventoryResponse response;
        if (inventory == null) {
            response = new InventoryResponse(status, getInfo(), null, hasMore);
        } else {
            final Map<Purchase, VerificationResult> verifiedRevertedInventory = new HashMap<>();
            for (final Purchase purchase : inventory) {
                final VerificationResult result = purchaseVerifier.verify(purchase);
                final Purchase revertedPurchase = OPFIabUtils.revert(skuResolver, purchase);
                verifiedRevertedInventory.put(revertedPurchase, result);
            }
            response = new InventoryResponse(status, getInfo(), verifiedRevertedInventory, hasMore);
        }
        postResponse(response);
    }

    protected void postPurchaseResponse(@NonNull final Status status,
                                        @Nullable final Purchase purchase) {
        final PurchaseResponse response;
        if (purchase == null) {
            response = new PurchaseResponse(status, getInfo(), null, null);
        } else {
            final VerificationResult result = purchaseVerifier.verify(purchase);
            final Purchase revertedPurchase = OPFIabUtils.revert(skuResolver, purchase);
            response = new PurchaseResponse(status, getInfo(), revertedPurchase, result);
        }
        postResponse(response);
    }

    protected void postConsumeResponse(@NonNull final Status status,
                                       @NonNull final Purchase purchase) {
        final Purchase revertedPurchase = OPFIabUtils.revert(skuResolver, purchase);
        postResponse(new ConsumeResponse(status, getInfo(), revertedPurchase));
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
        final int requestCode = event.getRequestCode();
        if (requestCode == this.requestCode) {
            final Activity activity = event.getActivity();
            final int resultCode = event.getResultCode();
            final Intent data = event.getData();
            onActivityResult(activity, requestCode, resultCode, data);
        }
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

    @SuppressWarnings("PMD.EmptyMethodInAbstractClassShouldBeAbstract")
    @Nullable
    @Override
    public Intent getStorePageIntent() {
        return null;
    }

    @SuppressWarnings("PMD.EmptyMethodInAbstractClassShouldBeAbstract")
    @Nullable
    @Override
    public Intent getRateIntent() {
        return null;
    }

    //CHECKSTYLE:OFF
    @SuppressWarnings({"PMD", "TypeMayBeWeakened", "RedundantIfStatement"})
    @Override
    public boolean equals(final Object o) {
        final BillingProviderInfo info = getInfo();
        if (this == o) return true;
        if (!(o instanceof BaseBillingProvider)) return false;

        final BaseBillingProvider that = (BaseBillingProvider) o;

        if (!info.equals(that.getInfo())) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return getInfo().hashCode();
    }

    @Override
    public String toString() {
        return getInfo().getName();
    }
    //CHECKSTYLE:ON


    public abstract static class Builder<RESOLVER extends SkuResolver, VERIFIER extends PurchaseVerifier> {

        @NonNull
        protected final Context context;
        @NonNull
        protected RESOLVER skuResolver;
        @NonNull
        protected VERIFIER purchaseVerifier;
        @Nullable
        protected Integer requestCode;

        protected Builder(@NonNull final Context context,
                          @NonNull final RESOLVER skuResolver,
                          @NonNull final VERIFIER purchaseVerifier) {
            this.context = context;
            this.skuResolver = skuResolver;
            this.purchaseVerifier = purchaseVerifier;
        }

        protected Builder setSkuResolver(@NonNull final RESOLVER skuResolver) {
            this.skuResolver = skuResolver;
            return this;
        }

        protected Builder setPurchaseVerifier(@NonNull final VERIFIER purchaseVerifier) {
            this.purchaseVerifier = purchaseVerifier;
            return this;
        }

        protected Builder setRequestCode(final int requestCode) {
            this.requestCode = requestCode;
            return this;
        }

        public abstract BaseBillingProvider build();
    }
}
