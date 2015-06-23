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

package org.onepf.opfiab.util;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import org.onepf.opfiab.model.billing.Purchase;
import org.onepf.opfiab.model.billing.SkuDetails;
import org.onepf.opfiab.model.event.billing.BillingEventType;
import org.onepf.opfiab.model.event.billing.BillingRequest;
import org.onepf.opfiab.model.event.billing.BillingResponse;
import org.onepf.opfiab.model.event.billing.ConsumeRequest;
import org.onepf.opfiab.model.event.billing.ConsumeResponse;
import org.onepf.opfiab.model.event.billing.InventoryResponse;
import org.onepf.opfiab.model.event.billing.PurchaseRequest;
import org.onepf.opfiab.model.event.billing.PurchaseResponse;
import org.onepf.opfiab.model.event.billing.SkuDetailsRequest;
import org.onepf.opfiab.model.event.billing.SkuDetailsResponse;
import org.onepf.opfiab.model.event.billing.Status;
import org.onepf.opfiab.sku.SkuResolver;
import org.onepf.opfiab.verification.PurchaseVerifier;
import org.onepf.opfiab.verification.VerificationResult;

import java.lang.ref.Reference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

public final class BillingUtils {

    /**
     * Constructs empty response corresponding to supplied request.
     *
     * @param providerName   Name of the provider handling request, can be null.
     * @param billingRequest Request to make response for.
     * @param status         Status for newly constructed response.
     *
     * @return Newly constructed BillingResponse with no data.
     */
    @SuppressFBWarnings({"BC_UNCONFIRMED_CAST"})
    @NonNull
    public static BillingResponse emptyResponse(@Nullable final String providerName,
                                                @NonNull final BillingRequest billingRequest,
                                                @NonNull final Status status) {
        final BillingResponse billingResponse;
        switch (billingRequest.getType()) {
            case CONSUME:
                final ConsumeRequest consumeRequest = (ConsumeRequest) billingRequest;
                final Purchase purchase = consumeRequest.getPurchase();
                billingResponse = new ConsumeResponse(status, providerName, purchase);
                break;
            case PURCHASE:
                billingResponse = new PurchaseResponse(status, providerName);
                break;
            case SKU_DETAILS:
                billingResponse = new SkuDetailsResponse(status, providerName);
                break;
            case INVENTORY:
                billingResponse = new InventoryResponse(status, providerName);
                break;
            default:
                throw new IllegalArgumentException();
        }
        return billingResponse;
    }

    @Nullable
    public static Activity getActivity(@NonNull final BillingRequest request) {
        final Reference<Activity> reference = request.getActivity();
        return reference == null ? null : reference.get();
    }

    @NonNull
    public static BillingResponse verify(@NonNull final PurchaseVerifier verifier,
                                         @NonNull final BillingResponse response) {
        final Status status = response.getStatus();
        final BillingEventType type = response.getType();
        final String name = response.getProviderName();
        if (type == BillingEventType.PURCHASE) {
            final Purchase purchase = ((PurchaseResponse) response).getPurchase();
            if (purchase != null) {
                return new PurchaseResponse(status, name, purchase, verifier.verify(purchase));
            }
        } else if (type == BillingEventType.INVENTORY) {
            final InventoryResponse inventoryResponse = (InventoryResponse) response;
            final Map<Purchase, VerificationResult> inventory = inventoryResponse.getInventory();
            final Iterable<Purchase> purchases = inventory.keySet();
            final boolean hasMore = inventoryResponse.hasMore();
            return new InventoryResponse(status, name, verify(verifier, purchases), hasMore);
        }
        return response;
    }

    @NonNull
    public static Map<Purchase, VerificationResult> verify(
            @NonNull final PurchaseVerifier verifier,
            @NonNull final Iterable<Purchase> purchases) {
        final Map<Purchase, VerificationResult> verifiedPurchases = new HashMap<>();
        for (final Purchase purchase : purchases) {
            verifiedPurchases.put(purchase, verifier.verify(purchase));
        }
        return verifiedPurchases;
    }

    @NonNull
    public static SkuDetails substituteSku(@NonNull final SkuDetails skuDetails,
                                           @NonNull final String sku) {
        if (TextUtils.equals(skuDetails.getSku(), sku)) {
            return skuDetails;
        }
        return skuDetails.copyWithSku(sku);
    }

    @NonNull
    public static Purchase substituteSku(@NonNull final Purchase purchase,
                                         @NonNull final String sku) {
        if (TextUtils.equals(purchase.getSku(), sku)) {
            return purchase;
        }
        return purchase.copyWithSku(sku);
    }

    @NonNull
    public static BillingRequest resolve(@NonNull final SkuResolver resolver,
                                         @NonNull final BillingRequest request) {
        final BillingEventType type = request.getType();
        final Activity activity = getActivity(request);
        final boolean handlesResult = request.isActivityHandlesResult();
        if (type == BillingEventType.PURCHASE) {
            final PurchaseRequest purchaseRequest = (PurchaseRequest) request;
            final String sku = purchaseRequest.getSku();
            final String newSku = resolver.resolve(sku);
            return new PurchaseRequest(activity, handlesResult, newSku);
        } else if (type == BillingEventType.CONSUME) {
            final ConsumeRequest consumeRequest = (ConsumeRequest) request;
            final Purchase purchase = consumeRequest.getPurchase();
            final String sku = purchase.getSku();
            final String newSku = resolver.resolve(sku);
            final Purchase newPurchase = substituteSku(purchase, newSku);
            return new ConsumeRequest(activity, handlesResult, newPurchase);
        } else if (type == BillingEventType.SKU_DETAILS) {
            final SkuDetailsRequest skuDetailsRequest = (SkuDetailsRequest) request;
            final Collection<String> skus = skuDetailsRequest.getSkus();
            final Set<String> newSkus = resolve(resolver, skus);
            return new SkuDetailsRequest(activity, handlesResult, newSkus);
        }
        return request;
    }

    @NonNull
    public static BillingResponse revert(@NonNull final SkuResolver resolver,
                                         @NonNull final BillingResponse response) {
        final Status status = response.getStatus();
        final BillingEventType type = response.getType();
        final String name = response.getProviderName();
        if (type == BillingEventType.PURCHASE) {
            final PurchaseResponse purchaseResponse = (PurchaseResponse) response;
            final Purchase purchase = purchaseResponse.getPurchase();
            final VerificationResult verification = purchaseResponse.getVerificationResult();
            if (purchase != null) {
                final Purchase newPurchase = revert(resolver, purchase);
                return new PurchaseResponse(status, name, newPurchase, verification);
            }
        } else if (type == BillingEventType.CONSUME) {
            final ConsumeResponse consumeResponse = (ConsumeResponse) response;
            final Purchase newPurchase = revert(resolver, consumeResponse.getPurchase());
            return new ConsumeResponse(status, name, newPurchase);
        } else if (type == BillingEventType.SKU_DETAILS) {
            final SkuDetailsResponse skuDetailsResponse = (SkuDetailsResponse) response;
            final Collection<SkuDetails> skusDetails = skuDetailsResponse.getSkusDetails();
            return new SkuDetailsResponse(status, name, revert(resolver, skusDetails));
        } else if (type == BillingEventType.INVENTORY) {
            final InventoryResponse inventoryResponse = (InventoryResponse) response;
            final Map<Purchase, VerificationResult> inventory = inventoryResponse.getInventory();
            final boolean hasMore = inventoryResponse.hasMore();
            return new InventoryResponse(status, name, revert(resolver, inventory), hasMore);
        }
        return response;
    }

    @NonNull
    public static SkuDetails resolve(@NonNull final SkuResolver skuResolver,
                                     @NonNull final SkuDetails skuDetails) {
        final String resolvedSku = skuResolver.resolve(skuDetails.getSku());
        return substituteSku(skuDetails, resolvedSku);
    }

    @NonNull
    public static Purchase resolve(@NonNull final SkuResolver skuResolver,
                                   @NonNull final Purchase purchase) {
        final String resolvedSku = skuResolver.resolve(purchase.getSku());
        return substituteSku(purchase, resolvedSku);
    }

    @NonNull
    public static Set<String> resolve(@NonNull final SkuResolver resolver,
                                      @NonNull final Iterable<String> skus) {
        final Set<String> resolvedSkus = new HashSet<>();
        for (final String sku : skus) {
            resolvedSkus.add(resolver.resolve(sku));
        }
        return resolvedSkus;
    }

    @NonNull
    public static SkuDetails revert(@NonNull final SkuResolver skuResolver,
                                    @NonNull final SkuDetails skuDetails) {
        final String resolvedSku = skuResolver.revert(skuDetails.getSku());
        return substituteSku(skuDetails, resolvedSku);
    }

    @NonNull
    public static Purchase revert(@NonNull final SkuResolver skuResolver,
                                  @NonNull final Purchase purchase) {
        final String resolvedSku = skuResolver.revert(purchase.getSku());
        return substituteSku(purchase, resolvedSku);
    }

    @NonNull
    public static Collection<SkuDetails> revert(@NonNull final SkuResolver resolver,
                                                @NonNull final Iterable<SkuDetails> skusDetails) {
        final Collection<SkuDetails> newSkusDetails = new ArrayList<>();
        for (final SkuDetails skuDetails : skusDetails) {
            newSkusDetails.add(revert(resolver, skuDetails));
        }
        return newSkusDetails;
    }

    @NonNull
    public static Map<Purchase, VerificationResult> revert(
            @NonNull final SkuResolver resolver,
            @NonNull final Map<Purchase, VerificationResult> inventory) {
        if (inventory.isEmpty()) {
            return inventory;
        }
        final Map<Purchase, VerificationResult> newInventory = new HashMap<>();
        for (final Map.Entry<Purchase, VerificationResult> entry : inventory.entrySet()) {
            newInventory.put(revert(resolver, entry.getKey()), entry.getValue());
        }
        return newInventory;
    }


    private BillingUtils() {
        throw new UnsupportedOperationException();
    }
}
