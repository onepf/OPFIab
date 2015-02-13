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
import android.app.ActivityManager;
import android.content.ComponentName;
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
import org.onepf.opfiab.model.event.billing.Request;
import org.onepf.opfiab.model.event.billing.Response;
import org.onepf.opfiab.model.event.billing.SkuDetailsResponse;
import org.onepf.opfiab.sku.SkuResolver;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import static android.content.Context.ACTIVITY_SERVICE;

public final class OPFIabUtils {

    private OPFIabUtils() {
        throw new UnsupportedOperationException();
    }

    public static boolean isOnTopOfTask(@NonNull final Activity activity) {
        final ActivityManager am = (ActivityManager) activity.getSystemService(ACTIVITY_SERVICE);
        final List<ActivityManager.RunningTaskInfo> tasks;
        //        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
        tasks = am.getRunningTasks(1);
        if (!tasks.isEmpty()) {
            final ComponentName componentName = tasks.get(0).topActivity;
            return componentName == activity.getComponentName();
        }
        //        }
        return false;
    }

    @SuppressFBWarnings({"BC_UNCONFIRMED_CAST"})
    public static Response emptyResponse(@Nullable final BillingProviderInfo providerInfo,
                                         @NonNull final Request request,
                                         @NonNull final Response.Status status) {
        final Response response;
        final BillingEvent.Type type = request.getType();
        switch (type) {
            case CONSUME:
                response = new ConsumeResponse(providerInfo, request, status);
                break;
            case PURCHASE:
                response = new PurchaseResponse(providerInfo, request, status, null);
                break;
            case SKU_DETAILS:
                response = new SkuDetailsResponse(providerInfo, request, status, null);
                break;
            case INVENTORY:
                response = new InventoryResponse(providerInfo, request, status, null);
                break;
            default:
                throw new IllegalStateException();
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

    public static SkuDetails resolve(@NonNull final SkuDetails skuDetails,
                                     @NonNull final SkuResolver skuResolver) {
        final String resolvedSku = skuResolver.resolve(skuDetails.getSku());
        return substituteSku(skuDetails, resolvedSku);
    }

    public static Purchase resolve(@NonNull final Purchase purchase,
                                   @NonNull final SkuResolver skuResolver) {
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

    public static SkuDetails revert(@NonNull final SkuDetails skuDetails,
                                    @NonNull final SkuResolver skuResolver) {
        final String resolvedSku = skuResolver.revert(skuDetails.getSku());
        return substituteSku(skuDetails, resolvedSku);
    }

    public static Purchase revert(@NonNull final Purchase purchase,
                                  @NonNull final SkuResolver skuResolver) {
        final String resolvedSku = skuResolver.resolve(purchase.getSku());
        return substituteSku(purchase, resolvedSku);
    }
}
