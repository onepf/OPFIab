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

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import org.json.JSONException;
import org.onepf.opfiab.model.BillingProviderInfo;
import org.onepf.opfiab.model.billing.Purchase;
import org.onepf.opfiab.model.billing.SkuDetails;
import org.onepf.opfiab.model.event.billing.BillingRequest;
import org.onepf.opfiab.model.event.billing.BillingResponse;
import org.onepf.opfiab.model.event.billing.ConsumeRequest;
import org.onepf.opfiab.model.event.billing.ConsumeResponse;
import org.onepf.opfiab.model.event.billing.InventoryResponse;
import org.onepf.opfiab.model.event.billing.PurchaseResponse;
import org.onepf.opfiab.model.event.billing.SkuDetailsResponse;
import org.onepf.opfiab.model.event.billing.Status;
import org.onepf.opfiab.sku.SkuResolver;
import org.onepf.opfutils.OPFLog;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public final class OPFIabUtils {

    private static final int JSON_SPACES = 4;


    private OPFIabUtils() {
        throw new UnsupportedOperationException();
    }


    @NonNull
    public static String toString(@NonNull final JsonCompatible jsonCompatible) {
        try {
            return jsonCompatible.toJson().toString(JSON_SPACES);
        } catch (JSONException exception) {
            OPFLog.e("", exception);
        }
        return "";
    }

    @NonNull
    public static BillingResponse emptyResponse(@Nullable final BillingProviderInfo providerInfo,
                                                @NonNull final BillingRequest billingRequest,
                                                @NonNull final Status status) {
        final BillingResponse billingResponse;
        switch (billingRequest.getType()) {
            case CONSUME:
                final ConsumeRequest consumeRequest = (ConsumeRequest) billingRequest;
                final Purchase purchase = consumeRequest.getPurchase();
                billingResponse = new ConsumeResponse(status, providerInfo, purchase);
                break;
            case PURCHASE:
                billingResponse = new PurchaseResponse(status, providerInfo, null, null);
                break;
            case SKU_DETAILS:
                billingResponse = new SkuDetailsResponse(status, providerInfo, null);
                break;
            case INVENTORY:
                billingResponse = new InventoryResponse(status, providerInfo, null, false);
                break;
            default:
                throw new IllegalArgumentException();
        }
        return billingResponse;
    }

    public static SkuDetails substituteSku(@NonNull final SkuDetails skuDetails,
                                           @NonNull final String sku) {
        if (TextUtils.equals(skuDetails.getSku(), sku)) {
            return skuDetails;
        }
        final SkuDetails.Builder builder = new SkuDetails.Builder(sku);
        builder.setSkuDetails(skuDetails);
        return builder.build();
    }

    public static Purchase substituteSku(@NonNull final Purchase purchase,
                                         @NonNull final String sku) {
        if (TextUtils.equals(purchase.getSku(), sku)) {
            return purchase;
        }
        final Purchase.Builder builder = new Purchase.Builder(sku);
        builder.setPurchase(purchase);
        return builder.build();
    }

    public static SkuDetails resolve(@NonNull final SkuResolver skuResolver,
                                     @NonNull final SkuDetails skuDetails) {
        final String resolvedSku = skuResolver.resolve(skuDetails.getSku());
        return substituteSku(skuDetails, resolvedSku);
    }

    public static Purchase resolve(@NonNull final SkuResolver skuResolver,
                                   @NonNull final Purchase purchase) {
        final String resolvedSku = skuResolver.resolve(purchase.getSku());
        return substituteSku(purchase, resolvedSku);
    }

    public static Set<String> resolveSkus(@NonNull final SkuResolver resolver,
                                          @NonNull final Collection<String> skus) {
        final Set<String> resolvedSkus = new HashSet<>();
        for (final String sku : skus) {
            resolvedSkus.add(resolver.resolve(sku));
        }
        return resolvedSkus;
    }

    public static SkuDetails revert(@NonNull final SkuResolver skuResolver,
                                    @NonNull final SkuDetails skuDetails) {
        final String resolvedSku = skuResolver.revert(skuDetails.getSku());
        return substituteSku(skuDetails, resolvedSku);
    }

    public static Purchase revert(@NonNull final SkuResolver skuResolver,
                                  @NonNull final Purchase purchase) {
        final String resolvedSku = skuResolver.resolve(purchase.getSku());
        return substituteSku(purchase, resolvedSku);
    }
}
