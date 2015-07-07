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

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.onepf.opfiab.OPFIab;
import org.onepf.opfiab.model.billing.Purchase;
import org.onepf.opfiab.model.billing.SkuType;
import org.onepf.opfiab.model.event.ActivityResultRequest;
import org.onepf.opfiab.model.event.android.ActivityResult;
import org.onepf.opfiab.model.event.billing.BillingRequest;
import org.onepf.opfiab.model.event.billing.BillingResponse;
import org.onepf.opfiab.model.event.billing.ConsumeRequest;
import org.onepf.opfiab.model.event.billing.InventoryRequest;
import org.onepf.opfiab.model.event.billing.PurchaseRequest;
import org.onepf.opfiab.model.event.billing.SkuDetailsRequest;
import org.onepf.opfiab.model.event.billing.Status;
import org.onepf.opfiab.sku.SkuResolver;
import org.onepf.opfiab.util.ActivityForResultLauncher;
import org.onepf.opfiab.util.BillingUtils;
import org.onepf.opfiab.util.SyncedReference;
import org.onepf.opfiab.verification.PurchaseVerifier;
import org.onepf.opfutils.OPFLog;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import static org.onepf.opfiab.model.event.billing.Status.BILLING_UNAVAILABLE;
import static org.onepf.opfiab.model.event.billing.Status.ITEM_UNAVAILABLE;

/**
 * Base implementation of {@link BillingProvider}.
 * <p/>
 * Most implementations should extend this one unless implementation from scratch is absolutely necessary.
 *
 * @param <R> {@link SkuResolver} subclass to use with this BillingProvider.
 * @param <V> {@link PurchaseVerifier} subclass to use with this BillingProvider.
 */
public abstract class BaseBillingProvider<R extends SkuResolver, V extends PurchaseVerifier>
        implements BillingProvider {

    protected static final int DEFAULT_REQUEST_CODE = 4232;


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
     * <p/>
     * At this point all SKUs should be resolved with provided {@link SkuResolver}.
     */
    protected abstract void skuDetails(@NonNull final SkuDetailsRequest request);

    /**
     * Loads user's inventory.
     */
    protected abstract void inventory(@NonNull final InventoryRequest request);

    /**
     * Purchase specified SKU.
     * <p/>
     * At this point sku should be already resolved with supplied {@link SkuResolver}.
     */
    protected abstract void purchase(@NonNull final PurchaseRequest request);

    /**
     * Consumes specified Purchase.
     * <p/>
     * SKU available from {@link Purchase#getSku()} should be already resolved with supplied
     * {@link SkuResolver}.
     */
    protected abstract void consume(@NonNull final ConsumeRequest request);

    @Nullable
    protected ActivityResult requestActivityResult(
            @NonNull final BillingRequest billingRequest,
            @NonNull final ActivityForResultLauncher launcher) {
        final SyncedReference<ActivityResult> syncResult = new SyncedReference<>();
        OPFIab.post(new ActivityResultRequest(billingRequest, launcher, syncResult));
        OPFLog.d("Waiting for ActivityResult");
        return syncResult.get();
    }

    /**
     * Entry point for all incoming billing requests.
     * <p/>
     * Might be a good place for intercepting request.
     *
     * @param billingRequest incoming BillingRequest object.
     */
    @SuppressFBWarnings({"BC_UNCONFIRMED_CAST"})
    protected void handleRequest(@NonNull final BillingRequest billingRequest) {
        final BillingRequest resolvedRequest = BillingUtils.resolve(skuResolver, billingRequest);
        switch (resolvedRequest.getType()) {
            case CONSUME:
                final ConsumeRequest consumeRequest = (ConsumeRequest) resolvedRequest;
                final Purchase purchase = consumeRequest.getPurchase();
                final String purchaseProviderName = purchase.getProviderName();
                final String providerName = getName();
                if (!providerName.equals(purchaseProviderName)) {
                    OPFLog.e("Attempt to consume purchase from wrong provider: %s.\n"
                            + "Current provider: %s", purchaseProviderName, providerName);
                    postEmptyResponse(resolvedRequest, ITEM_UNAVAILABLE);
                    break;
                }
                consume(consumeRequest);
                break;
            case PURCHASE:
                final PurchaseRequest purchaseRequest = (PurchaseRequest) resolvedRequest;
                purchase(purchaseRequest);
                break;
            case SKU_DETAILS:
                final SkuDetailsRequest skuDetailsRequest = (SkuDetailsRequest) resolvedRequest;
                skuDetails(skuDetailsRequest);
                break;
            case INVENTORY:
                final InventoryRequest inventoryRequest = (InventoryRequest) resolvedRequest;
                inventory(inventoryRequest);
                break;
            default:
                throw new IllegalStateException();
        }
    }

    @Override
    public void onBillingRequest(@NonNull final BillingRequest billingRequest) {
        if (!isAvailable()) {
            postEmptyResponse(billingRequest, BILLING_UNAVAILABLE);
        } else {
            handleRequest(billingRequest);
        }
    }


    protected BillingResponse verify(@NonNull final BillingResponse response) {
        return BillingUtils.verify(purchaseVerifier, response);
    }

    protected BillingResponse revertSku(@NonNull final BillingResponse response) {
        return BillingUtils.revert(skuResolver, response);
    }

    /**
     * Notifies library about billing response from this billing provider.
     *
     * @param billingResponse BillingResponse object to send to library.
     */
    protected void postResponse(@NonNull final BillingResponse billingResponse) {
        final BillingResponse verifiedResponse = verify(billingResponse);
        final BillingResponse revertedResponse = revertSku(verifiedResponse);
        OPFIab.post(revertedResponse);
    }

    /**
     * Constructs and sends empty {@link BillingResponse}.
     *
     * @param billingRequest BillingRequest object to construct corresponding response to.
     * @param status         Status object to use in BillingResponse.
     */
    protected void postEmptyResponse(@NonNull final BillingRequest billingRequest,
                                     @NonNull final Status status) {
        postResponse(BillingUtils.emptyResponse(getName(), billingRequest, status));
    }

    @Override
    public boolean skuTypeSupported(@NonNull final SkuType skuType) {
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
        final String name = getName();
        if (this == o) return true;
        if (!(o instanceof BaseBillingProvider)) return false;

        final BaseBillingProvider that = (BaseBillingProvider) o;

        if (!name.equals(that.getName())) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    @Override
    public String toString() {
        return getName();
    }
    //CHECKSTYLE:ON
}
