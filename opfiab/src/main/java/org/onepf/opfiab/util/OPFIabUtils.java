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
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import org.json.JSONException;
import org.onepf.opfiab.android.OPFIabActivity;
import org.onepf.opfiab.billing.ActivityBillingProvider;
import org.onepf.opfiab.billing.BillingProvider;
import org.onepf.opfiab.model.JsonCompatible;
import org.onepf.opfiab.model.billing.Purchase;
import org.onepf.opfiab.model.billing.SkuDetails;
import org.onepf.opfiab.model.event.billing.BillingRequest;
import org.onepf.opfiab.model.event.billing.BillingResponse;
import org.onepf.opfiab.model.event.billing.ConsumeRequest;
import org.onepf.opfiab.model.event.billing.ConsumeResponse;
import org.onepf.opfiab.model.event.billing.InventoryResponse;
import org.onepf.opfiab.model.event.billing.PurchaseRequest;
import org.onepf.opfiab.model.event.billing.PurchaseResponse;
import org.onepf.opfiab.model.event.billing.SkuDetailsResponse;
import org.onepf.opfiab.model.event.billing.Status;
import org.onepf.opfiab.sku.SkuResolver;
import org.onepf.opfutils.OPFLog;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import static android.content.pm.PackageManager.GET_SIGNATURES;


/**
 * Collection of handy utility methods.
 * <p/>
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

    @NonNull
    public static String toString(@NonNull final InputStream inputStream) {
        final BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        final StringBuilder builder = new StringBuilder();
        try {
            for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                builder.append(line);
            }
            return builder.toString();
        } catch (IOException exception) {
            OPFLog.e("", exception);
        }
        return "";
    }

    /**
     * Indicates whether this activity was started by library to handle some specific action.
     *
     * @param activity Activity object to check.
     *
     * @return True if activity was started by library, false otherwise.
     *
     * @see PurchaseRequest
     * @see ActivityBillingProvider
     */
    public static boolean isActivityFake(@NonNull final Activity activity) {
        return activity.getClass() == OPFIabActivity.class;
    }

    /**
     * Retrieves signature form supplied package.
     *
     * @param context     Context object to get {@link PackageManager} from.
     * @param packageName Package to retrieve signature for.
     *
     * @return Signature object if package found, null otherwise.
     */
    @SuppressWarnings("PackageManagerGetSignatures")
    @NonNull
    public static Signature[] getPackageSignatures(@NonNull final Context context,
                                                   @NonNull final String packageName) {
        final PackageManager packageManager = context.getPackageManager();
        try {
            final PackageInfo info = packageManager.getPackageInfo(packageName, GET_SIGNATURES);
            final Signature[] signatures = info.signatures;
            if (signatures != null) {
                return signatures;
            }
        } catch (PackageManager.NameNotFoundException exception) {
            OPFLog.e("", exception);
        }
        return new Signature[0];
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
                billingResponse = new PurchaseResponse(status, providerName, null, null);
                break;
            case SKU_DETAILS:
                billingResponse = new SkuDetailsResponse(status, providerName, null);
                break;
            case INVENTORY:
                billingResponse = new InventoryResponse(status, providerName, null, false);
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
        return skuDetails.copyWithSku(sku);
    }

    public static Purchase substituteSku(@NonNull final Purchase purchase,
                                         @NonNull final String sku) {
        if (TextUtils.equals(purchase.getSku(), sku)) {
            return purchase;
        }
        return purchase.copyWithSku(sku);
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
