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

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import org.onepf.opfiab.model.BillingProviderInfo;
import org.onepf.opfiab.model.billing.Purchase;
import org.onepf.opfiab.model.billing.SkuDetails;
import org.onepf.opfiab.model.event.BillingEvent;
import org.onepf.opfiab.model.event.billing.ConsumeResponse;
import org.onepf.opfiab.model.event.billing.InventoryResponse;
import org.onepf.opfiab.model.event.billing.PurchaseResponse;
import org.onepf.opfiab.model.event.billing.Response;
import org.onepf.opfiab.model.event.billing.SkuDetailsResponse;
import org.onepf.opfiab.sku.SkuResolver;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public final class OPFIabUtils {

    private OPFIabUtils() {
        throw new UnsupportedOperationException();
    }

    public static boolean isConnected(@NonNull final Context context) {
        final Object service = context.getSystemService(Context.CONNECTIVITY_SERVICE);
        final ConnectivityManager cm = (ConnectivityManager) service;
        final NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    public static Response emptyResponse(@Nullable final BillingProviderInfo providerInfo,
                                         @NonNull final BillingEvent.Type type,
                                         @NonNull final Response.Status status) {
        final Response response;
        switch (type) {
            case CONSUME:
                response = new ConsumeResponse(providerInfo, status);
                break;
            case PURCHASE:
                response = new PurchaseResponse(providerInfo, status, null);
                break;
            case SKU_DETAILS:
                response = new SkuDetailsResponse(providerInfo, status, null);
                break;
            case INVENTORY:
                response = new InventoryResponse(providerInfo, status, null, false);
                break;
            default:
                throw new IllegalArgumentException(String.valueOf(type));
        }
        return response;
    }

    public static SkuDetails substituteSku(@NonNull final SkuDetails skuDetails,
                                           @NonNull final String sku) {
        if (TextUtils.equals(skuDetails.getSku(), sku)) {
            return skuDetails;
        }
        final SkuDetails.Builder builder = new SkuDetails.Builder(sku);
        builder.setType(skuDetails.getType());
        builder.setJson(skuDetails.getJson());
        builder.setPrice(skuDetails.getPrice());
        builder.setTitle(skuDetails.getTitle());
        builder.setDescription(skuDetails.getDescription());
        builder.setIconUrl(skuDetails.getIconUrl());
        return builder.build();
    }

    public static Purchase substituteSku(@NonNull final Purchase purchase,
                                         @NonNull final String sku) {
        if (TextUtils.equals(purchase.getSku(), sku)) {
            return purchase;
        }
        final Purchase.Builder builder = new Purchase.Builder(sku);
        builder.setType(purchase.getType());
        builder.setJson(purchase.getJson());
        builder.setToken(purchase.getToken());
        builder.setPurchaseTime(purchase.getPurchaseTime());
        builder.setCanceled(purchase.isCanceled());
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
