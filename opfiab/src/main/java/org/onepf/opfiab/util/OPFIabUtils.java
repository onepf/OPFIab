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

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import org.json.JSONException;
import org.onepf.opfiab.billing.BillingProvider;
import org.onepf.opfiab.model.BillingProviderInfo;
import org.onepf.opfiab.model.JsonCompatible;
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
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;


/**
 * Collection of handy utility methods.
 * <br>
 * Intended for internal use.
 */
public final class OPFIabUtils {

    private static final String KEY_REQUEST = OPFIabUtils.class.getName() + ".request";
    private static final int JSON_SPACES = 4;


    private OPFIabUtils() {
        throw new UnsupportedOperationException();
    }


    /**
     * Converts supplied object to human-readable JSON representation.
     *
     * @param jsonCompatible Object to convert.
     *
     * @return Human-readable string, can't be null.
     */
    @NonNull
    public static String toString(@NonNull final JsonCompatible jsonCompatible) {
        try {
            return jsonCompatible.toJson().toString(JSON_SPACES);
        } catch (JSONException exception) {
            OPFLog.e("", exception);
        }
        return "";
    }

    /**
     * Filters out unavailable {@link BillingProvider}s.
     *
     * @param providers Providers to filter.
     *
     * @return Collection of available providers.
     */
    @NonNull
    public static Iterable<BillingProvider> getAvailable(
            @NonNull final Iterable<BillingProvider> providers) {
        final Collection<BillingProvider> availableProviders = new LinkedHashSet<>();
        for (final BillingProvider provider : providers) {
            if (provider.isAvailable()) {
                availableProviders.add(provider);
            }
        }
        return availableProviders;
    }

    /**
     * Looks for a provider with supplied {@link BillingProviderInfo}.
     *
     * @param providers Providers to look among.
     * @param info      Info to look up.
     *
     * @return BillingProvider if it was found, null otherwise.
     */
    @Nullable
    public static BillingProvider findWithInfo(@NonNull final Iterable<BillingProvider> providers,
                                               @NonNull final BillingProviderInfo info) {
        for (final BillingProvider billingProvider : providers) {
            if (info.equals(billingProvider.getInfo())) {
                return billingProvider;
            }
        }
        return null;
    }

    // where are you stream API...

    /**
     * Looks for a provider with supplied installer.
     *
     * @param providers   Providers to look among.
     * @param packageName Installer to look for.
     *
     * @return BillingProvider if it was found, null otherwise.
     */
    @Nullable
    public static BillingProvider withInstaller(
            @NonNull final Iterable<BillingProvider> providers,
            @NonNull final String packageName) {
        for (final BillingProvider billingProvider : providers) {
            final BillingProviderInfo info = billingProvider.getInfo();
            if (packageName.equals(info.getInstaller())) {
                return billingProvider;
            }
        }
        return null;
    }

    /**
     * Constructs empty response corresponding to supplied request.`
     *
     * @param providerInfo   Info of provider handling request, can be null.
     * @param billingRequest Request to make response for.
     * @param status         Status for newly constructed response.
     *
     * @return Newly constructed BillingResponse with no data.
     */
    @SuppressFBWarnings({"BC_UNCONFIRMED_CAST"})
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
                                          @NonNull final Iterable<String> skus) {
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
        final String resolvedSku = skuResolver.revert(purchase.getSku());
        return substituteSku(purchase, resolvedSku);
    }

    @SuppressWarnings("TypeMayBeWeakened")
    public static void putRequest(@NonNull final Bundle bundle,
                                  @NonNull final BillingRequest request) {
        bundle.putSerializable(KEY_REQUEST, request);
    }

    @Nullable
    public static BillingRequest getRequest(@Nullable final Bundle bundle) {
        if (bundle != null && bundle.containsKey(KEY_REQUEST)) {
            return (BillingRequest) bundle.getSerializable(KEY_REQUEST);
        }
        return null;
    }

    /**
     * Removes first element from supplied collection.
     *
     * @param collection Collection to remove element from.
     *
     * @return Removed object or null.
     */
    @Nullable
    public static <E> E poll(@NonNull final Collection<E> collection) {
        if (collection.isEmpty()) {
            return null;
        }
        final Iterator<E> iterator = collection.iterator();
        final E e = iterator.next();
        iterator.remove();
        return e;
    }
}
