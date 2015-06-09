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

import org.onepf.opfiab.ActivityMonitor;
import org.onepf.opfiab.OPFIab;
import org.onepf.opfiab.model.billing.Purchase;
import org.onepf.opfiab.model.billing.SkuDetails;
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
import org.onepf.opfiab.util.OPFIabUtils;
import org.onepf.opfiab.verification.PurchaseVerifier;
import org.onepf.opfiab.verification.VerificationResult;
import org.onepf.opfutils.OPFLog;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import static org.onepf.opfiab.model.event.billing.Status.BILLING_UNAVAILABLE;
import static org.onepf.opfiab.model.event.billing.Status.ITEM_UNAVAILABLE;
import static org.onepf.opfiab.model.event.billing.Status.USER_CANCELED;

/**
 * Base implementation of {@link BillingProvider}.
 * <br>
 * Most implementations should extend this one unless implementation from scratch is absolutely necessary.
 *
 * @param <R> {@link SkuResolver} subclass to use with this BillingProvider.
 * @param <V> {@link PurchaseVerifier} subclass to use with this BillingProvider.
 */
public abstract class BaseBillingProvider<R extends SkuResolver, V extends PurchaseVerifier>
        implements BillingProvider {

    @NonNull
    protected final Context context;
    @NonNull
    protected final R skuResolver;
    @NonNull
    protected final V purchaseVerifier;

    protected BaseBillingProvider(@NonNull final Context context,
                                  @NonNull final R skuResolver,
                                  @NonNull final V purchaseVerifier) {
        this.context = context.getApplicationContext();
        this.purchaseVerifier = purchaseVerifier;
        this.skuResolver = skuResolver;
    }

    /**
     * Loads details for specified SKUs.
     * <br>
     * At this point all SKUs should be resolved with provided {@link SkuResolver}.
     *
     * @param skus skus to load details for.
     * @see SkuDetails
     * @see #postSkuDetailsResponse(Status, Collection)
     */
    protected abstract void skuDetails(@NonNull final Set<String> skus);

    /**
     * Loads user's inventory.
     *
     * @param startOver Flag indicating whether inventory should be loaded from the start or
     *                  continue from the last request.
     * @see Purchase
     * @see #postInventoryResponse(Status, Iterable, boolean)
     */
    protected abstract void inventory(final boolean startOver);

    /**
     * Purchase specified SKU.
     * <br>
     * At this point sku should be already resolved with supplied {@link SkuResolver}.
     *
     * @param sku      SKU to purchase.
     * @see Purchase
     * @see #postResponse(BillingResponse)
     */
    protected abstract void purchase(@NonNull final String sku);

    /**
     * Consumes specified Purchase.
     * <br>
     * SKU available from {@link Purchase#getSku()} should be already resolved with supplied
     * {@link SkuResolver}.
     *
     * @param purchase Purchase object to consume
     * @see #postConsumeResponse(Status, Purchase)
     */
    protected abstract void consume(@NonNull final Purchase purchase);

    /**
     * Entry point for all incoming billing requests.
     * <br>
     * Might be a good place for intercepting request.
     *
     * @param billingRequest incoming BillingRequest object.
     */
    @SuppressFBWarnings({"BC_UNCONFIRMED_CAST"})
    protected void handleRequest(@NonNull final BillingRequest billingRequest) {
        OPFLog.logMethod(billingRequest);
        final Activity activity = billingRequest.getActivity();
        if (activity != null && !ActivityMonitor.isResumed(activity)) {
            postEmptyResponse(billingRequest, USER_CANCELED);
            return;
        }
        final String resolvedSku;
        switch (billingRequest.getType()) {
            case CONSUME:
                final ConsumeRequest consumeRequest = (ConsumeRequest) billingRequest;
                final Purchase purchase = consumeRequest.getPurchase();
                final String purchaseProviderName = purchase.getProviderName();
                final String providerName = getName();
                if (!providerName.equals(purchaseProviderName)) {
                    OPFLog.e("Attempt to consume purchase from wrong provider: %s.\n"
                                     + "Current provider: %s", purchaseProviderName, providerName);
                    postEmptyResponse(billingRequest, ITEM_UNAVAILABLE);
                    break;
                }
                resolvedSku = skuResolver.resolve(purchase.getSku());
                consume(OPFIabUtils.substituteSku(purchase, resolvedSku));
                break;
            case PURCHASE:
                final PurchaseRequest purchaseRequest = (PurchaseRequest) billingRequest;
                resolvedSku = skuResolver.resolve(purchaseRequest.getSku());
                purchase(resolvedSku);
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

    /**
     * Notifies library about billing response from this billing provider.
     *
     * @param billingResponse BillingResponse object to send to library.
     */
    protected void postResponse(@NonNull final BillingResponse billingResponse) {
        OPFIab.post(billingResponse);
    }

    /**
     * Constructs and sends empty {@link BillingResponse}.
     *
     * @param billingRequest BillingRequest object to construct corresponding response to.
     * @param status         Status object to use in BillingResponse.
     * @see #postResponse(BillingResponse)
     */
    protected void postEmptyResponse(@NonNull final BillingRequest billingRequest,
                                     @NonNull final Status status) {
        postResponse(OPFIabUtils.emptyResponse(getName(), billingRequest, status));
    }

    /**
     * Constructs and sends {@link SkuDetailsResponse}.
     * <br>
     * SKUs available from {@link SkuDetails#getSku()} will be reverted with supplied
     * {@link SkuResolver}.
     *
     * @param status      Status object to use in response.
     * @param skusDetails Can be null. Collection of SkuDetails objects to add in response.
     * @see SkuDetailsResponse
     */
    protected void postSkuDetailsResponse(@NonNull final Status status,
                                          @Nullable final Collection<SkuDetails> skusDetails) {
        final SkuDetailsResponse response;
        if (skusDetails == null) {
            response = new SkuDetailsResponse(status, getName(), null);
        } else {
            final Collection<SkuDetails> revertedSkusDetails = new ArrayList<>(skusDetails.size());
            for (final SkuDetails skuDetails : skusDetails) {
                revertedSkusDetails.add(OPFIabUtils.revert(skuResolver, skuDetails));
            }
            response = new SkuDetailsResponse(status, getName(), revertedSkusDetails);
        }
        postResponse(response);
    }

    /**
     * Constructs and sends {@link InventoryResponse}.
     * <br>
     * SKUs available from {@link Purchase#getSku()} will be reverted with supplied
     * {@link SkuResolver}.
     *
     * @param status    Status object to use in response.
     * @param inventory Can be null. Collection of Purchase objects to add in response.
     * @param hasMore   Flag indicating whether more items are available in user inventory.
     * @see InventoryResponse
     */
    protected void postInventoryResponse(@NonNull final Status status,
                                         @Nullable final Iterable<Purchase> inventory,
                                         final boolean hasMore) {
        final InventoryResponse response;
        if (inventory == null) {
            response = new InventoryResponse(status, getName(), null, hasMore);
        } else {
            final Map<Purchase, VerificationResult> verifiedRevertedInventory = new HashMap<>();
            for (final Purchase purchase : inventory) {
                final VerificationResult result = purchaseVerifier.verify(purchase);
                final Purchase revertedPurchase = OPFIabUtils.revert(skuResolver, purchase);
                verifiedRevertedInventory.put(revertedPurchase, result);
            }
            response = new InventoryResponse(status, getName(), verifiedRevertedInventory, hasMore);
        }
        postResponse(response);
    }

    /**
     * Constructs and sends {@link PurchaseResponse}.
     * <br>
     * SKU available from {@link Purchase#getSku()} will be reverted with supplied
     * {@link SkuResolver}.
     *
     * @param status   Status object to use in response.
     * @param purchase Can be null. Purchase object to add in response.
     * @see PurchaseResponse
     */
    protected void postPurchaseResponse(@NonNull final Status status,
                                        @Nullable final Purchase purchase) {
        final PurchaseResponse response;
        if (purchase == null) {
            response = new PurchaseResponse(status, getName(), null, null);
        } else {
            final VerificationResult result = purchaseVerifier.verify(purchase);
            final Purchase revertedPurchase = OPFIabUtils.revert(skuResolver, purchase);
            response = new PurchaseResponse(status, getName(), revertedPurchase, result);
        }
        postResponse(response);
    }

    /**
     * Constructs and sends {@link ConsumeResponse}.
     * <br>
     * SKU available from {@link Purchase#getSku()} will be reverted with supplied
     * {@link SkuResolver}.
     *
     * @param status   Status object to use in response.
     * @param purchase Can't be null. Purchase object to add in response.
     */
    protected void postConsumeResponse(@NonNull final Status status,
                                       @NonNull final Purchase purchase) {
        final Purchase revertedPurchase = OPFIabUtils.revert(skuResolver, purchase);
        postResponse(new ConsumeResponse(status, getName(), revertedPurchase));
    }

    @Override
    public void onEventAsync(@NonNull final BillingRequest billingRequest) {
        if (!isAvailable()) {
            postEmptyResponse(billingRequest, BILLING_UNAVAILABLE);
        } else {
            handleRequest(billingRequest);
        }
        OPFIab.post(new RequestHandledEvent(billingRequest));
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
        final String name = getName();
        if (this == o) return true;
        if (!(o instanceof BaseBillingProvider)) return false;

        final BaseBillingProvider that = (BaseBillingProvider) o;

        if (!name.equals(that.getName())) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return getName().hashCode();
    }

    @Override
    public String toString() {
        return getName();
    }
    //CHECKSTYLE:ON


    /**
     * Builder class for this BillingProvider.
     *
     * @param <R> {@link SkuResolver} subclass to use with this BillingProvider.
     * @param <V> {@link PurchaseVerifier} subclass to use with this BillingProvider.
     */
    protected abstract static class Builder<R extends SkuResolver, V extends PurchaseVerifier> {

        @NonNull
        protected final Context context;
        @Nullable
        protected R skuResolver;
        @Nullable
        protected V purchaseVerifier;

        protected Builder(@NonNull final Context context,
                          @Nullable final R skuResolver,
                          @Nullable final V purchaseVerifier) {
            this.context = context;
            this.skuResolver = skuResolver;
            this.purchaseVerifier = purchaseVerifier;
        }

        /**
         * Sets {@link SkuResolver} to use with this BillingProvider.
         *
         * @param skuResolver SkuResolver to use with this BillingProvider.
         * @return this object.
         */
        protected Builder setSkuResolver(@NonNull final R skuResolver) {
            this.skuResolver = skuResolver;
            return this;
        }

        /**
         * Sets {@link PurchaseVerifier} to use with this BillingProvider.
         *
         * @param purchaseVerifier PurchaseVerifier to use with this BillingProvider.
         * @return this object.
         */
        protected Builder setPurchaseVerifier(@NonNull final V purchaseVerifier) {
            this.purchaseVerifier = purchaseVerifier;
            return this;
        }

        /**
         * Constructs a new {@link BillingProvider} object.
         *
         * @return new BillingProvider.
         */
        public abstract BaseBillingProvider build();
    }
}
