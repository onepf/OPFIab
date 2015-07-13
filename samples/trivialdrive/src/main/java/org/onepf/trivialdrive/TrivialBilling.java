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

package org.onepf.trivialdrive;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.onepf.opfiab.amazon.AmazonBillingProvider;
import org.onepf.opfiab.billing.BillingProvider;
import org.onepf.opfiab.google.GoogleBillingProvider;
import org.onepf.opfiab.google.SimpleGooglePurchaseVerifier;
import org.onepf.opfiab.model.Configuration;
import org.onepf.opfiab.model.billing.Purchase;
import org.onepf.opfiab.model.billing.SkuDetails;
import org.onepf.opfiab.model.billing.SkuType;
import org.onepf.opfiab.model.event.billing.InventoryResponse;
import org.onepf.opfiab.model.event.billing.PurchaseResponse;
import org.onepf.opfiab.model.event.billing.SkuDetailsResponse;
import org.onepf.opfiab.openstore.ApplandBillingProvider;
import org.onepf.opfiab.openstore.AptoideBillingProvider;
import org.onepf.opfiab.openstore.OpenStoreBillingProvider;
import org.onepf.opfiab.openstore.SlideMEBillingProvider;
import org.onepf.opfiab.samsung.BillingMode;
import org.onepf.opfiab.samsung.SamsungBillingProvider;
import org.onepf.opfiab.samsung.SamsungMapSkuResolver;
import org.onepf.opfiab.samsung.SamsungPurchaseVerifier;
import org.onepf.opfiab.sku.MapSkuResolver;
import org.onepf.opfiab.sku.TypedMapSkuResolver;
import org.onepf.opfiab.verification.SimplePublicKeyPurchaseVerifier;
import org.onepf.opfiab.verification.VerificationResult;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class TrivialBilling {

    private static final String NAME = "billing";

    private static final String KEY_FIRST_LAUNCH = "first_launch";
    private static final String KEY_HELPER = "helper";
    private static final String KEY_PROVIDERS = "providers";
    private static final String KEY_AUTO_RECOVER = "auto_recover";

    private static final String AMAZON_SKU_GAS = "org.onepf.opfiab.trivialdrive.sku_gas";
    private static final String AMAZON_SKU_PREMIUM = "org.onepf.opfiab.trivialdrive.sku_premium";
    private static final String AMAZON_SKU_SUBSCRIPTION
            = "org.onepf.opfiab.trivialdrive.sku_infinite_gas";

    private static final String GOOGLE_SKU_GAS = "sku_gas";
    @SuppressWarnings("SpellCheckingInspection")
    private static final String GOOGLE_SKU_PREMIUM = "sku_premum";
    private static final String GOOGLE_SKU_SUBSCRIPTION = "sku_infinite_gas";
    @SuppressWarnings("SpellCheckingInspection")
    private static final String GOOGLE_PLAY_KEY
            = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAsTvSvwlDqz/wNr5UXD/rNxl+hs1vbbhta0O3g+" +
            "NS+jChs9+zhRCZScvQT1QzsAg6GNPCyoDXpYa9WWcZQ7kC4scQYQ6pYUUQDNaTwEqDRbmkesx5iRxEqoD9LU" +
            "hhaOL55NbFUPhiypkMww0t2768fuyxRnmBl2RZdQvM+paMDEDU2CtUMqrx4St3huGkFSjWlYMrU5vKELoLu9" +
            "acThoMk9ErEOFBqb4dGBNswH5JRm68r/u7a2XzEoo40dXQQH2/5tMy3AQCzVakHnfcIQcZO0BkQOh4o52ahh" +
            "y3vcCUhauN61YA492k+DmKT5GgSH+KxwgK5dcorjbh94E9e03dZwIDAQAB";

    public static final String SAMSUNG_SKU_GAS = "gas";
    public static final String SAMSUNG_SKU_PREMIUM = "premium";
    public static final String SAMSUNG_SKU_SUBSCRIPTION = "subscription";
    public static final String SAMSUNG_GROUP_ID = "100000105550";

    public static final String YANDEX_SKU_GAS = "org.onepf.sample.trivialdrive.sku_gas";
    public static final String YANDEX_SKU_PREMIUM = "org.onepf.sample.trivialdrive.sku_premium";
    public static final String YANDEX_SKU_SUBSCRIPTION = "org.onepf.sample.trivialdrive.sku_infinite_gas";
    public static final String YANDEX_PUBLIC_KEY
            = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAs4SI/obW+q3dhsY3g5B6UggPcL5olWK8WY3tnT" +
            "a2k3i2U40jQuHRNNs8SqzdJeuoBLsKjaEsdTT0SJtEucOMZrprXMch97QtuLB4Mgu3Gs7USL6dM7NCUSoYrg" +
            "Ogw1Koi+ab+ZvFJkVMb9a2EjYzR3aP0k4xjKyG2gW1rIEMMepxHm22VFjEg6YxBy+ecwRrjqDJOAPJyH6uSl" +
            "8vUT8AKuG+hcCuYbNvlMdEZJo6MXJ9vPNf/qPHwMy5G+faEprL6zR+HaPfxEqN/d8rbrW0qnr8LpXJ+nPB3/" +
            "irBiMSZSqA222GC7m12sNNmNnNNlI397F3fRQSTzVSRZt14YdPzwIDAQAB";

    public static final String APPLAND_GAS = "appland.sku_gas";
    public static final String APPLAND_PREMIUM = "appland.sku_premium";
    public static final String APPLAND_SUBSCRIPTION = "appland.sku_infinite_gas";

    public static final String SLIDEME_GAS = "slideme.sku_gas";
    public static final String SLIDEME_PREMIUM = "slideme.sku_premium";
    public static final String SLIDEME_SUBSCRIPTION = "slideme.sku_infinite_gas";

    public static final String SKU_GAS = "sku_gas";
    public static final String SKU_PREMIUM = "sku_premium";
    public static final String SKU_SUBSCRIPTION = "sku_subscription";

    private static Context context;
    private static SharedPreferences preferences;

    // It's probably a good idea to store this values in a more secure way instead of boolean
    // Maybe some bits in int\long
    private static boolean premium;
    private static boolean subscription;
    private static final Map<String, SkuDetails> DETAILS = new HashMap<>();

    private static BillingProvider newProvider(final Provider provider) {
        switch (provider) {
            case AMAZON:
                return newAmazonProvider();
            case GOOGLE:
                return newGoogleProvider();
            case SAMSUNG:
                return newSamsungProvider();
            case YANDEX:
                return newYandexProvider();
            case APPLAND:
                return newApplandProvider();
            case APTOIDE:
                return newAptoideProvider();
            case SLIDEME:
                return newSlideMEProvider();
            case OPENSTORE:
                return newOpenStoreProvider();
            default:
                throw new IllegalStateException();
        }
    }


    private static BillingProvider newGoogleProvider() {
        final TypedMapSkuResolver skuResolver = new TypedMapSkuResolver();
        skuResolver.add(SKU_GAS, GOOGLE_SKU_GAS, SkuType.CONSUMABLE);
        skuResolver.add(SKU_PREMIUM, GOOGLE_SKU_PREMIUM, SkuType.ENTITLEMENT);
        skuResolver.add(SKU_SUBSCRIPTION, GOOGLE_SKU_SUBSCRIPTION, SkuType.SUBSCRIPTION);

        return new GoogleBillingProvider.Builder(context)
                .setPurchaseVerifier(new SimpleGooglePurchaseVerifier(GOOGLE_PLAY_KEY))
                .setSkuResolver(skuResolver)
                .build();
    }

    private static BillingProvider newAmazonProvider() {
        final MapSkuResolver skuResolver = new MapSkuResolver();
        skuResolver.add(SKU_GAS, AMAZON_SKU_GAS);
        skuResolver.add(SKU_PREMIUM, AMAZON_SKU_PREMIUM);
        skuResolver.add(SKU_SUBSCRIPTION, AMAZON_SKU_SUBSCRIPTION);

        return new AmazonBillingProvider.Builder(context)
                .setSkuResolver(skuResolver)
                .build();
    }

    private static BillingProvider newSamsungProvider() {
        final SamsungMapSkuResolver skuResolver = new SamsungMapSkuResolver(SAMSUNG_GROUP_ID);
        skuResolver.add(SKU_GAS, SAMSUNG_SKU_GAS, SkuType.CONSUMABLE);
        skuResolver.add(SKU_PREMIUM, SAMSUNG_SKU_PREMIUM, SkuType.ENTITLEMENT);
        skuResolver.add(SKU_SUBSCRIPTION, SAMSUNG_SKU_SUBSCRIPTION, SkuType.SUBSCRIPTION);

        return new SamsungBillingProvider.Builder(context)
                .setBillingMode(BillingMode.TEST_SUCCESS)
                .setPurchaseVerifier(new SamsungPurchaseVerifier(context, BillingMode.TEST_SUCCESS))
                .setSkuResolver(skuResolver)
                .build();
    }

    private static BillingProvider newYandexProvider() {
        final TypedMapSkuResolver skuResolver = new TypedMapSkuResolver();
        skuResolver.add(SKU_GAS, YANDEX_SKU_GAS, SkuType.CONSUMABLE);
        skuResolver.add(SKU_PREMIUM, YANDEX_SKU_PREMIUM, SkuType.ENTITLEMENT);
        skuResolver.add(SKU_SUBSCRIPTION, YANDEX_SKU_SUBSCRIPTION, SkuType.SUBSCRIPTION);

        return new OpenStoreBillingProvider.Builder(context)
                .setPurchaseVerifier(new SimplePublicKeyPurchaseVerifier(YANDEX_PUBLIC_KEY))
                .setSkuResolver(skuResolver)
                .build();
    }

    private static BillingProvider newAptoideProvider() {
        final TypedMapSkuResolver skuResolver = new TypedMapSkuResolver();
        //        skuResolver.add(SKU_GAS, YANDEX_SKU_GAS, SkuType.CONSUMABLE);
        //        skuResolver.add(SKU_PREMIUM, YANDEX_SKU_PREMIUM, SkuType.ENTITLEMENT);
        //        skuResolver.add(SKU_SUBSCRIPTION, YANDEX_SKU_SUBSCRIPTION, SkuType.SUBSCRIPTION);
        return new AptoideBillingProvider.Builder(context)
                .setSkuResolver(skuResolver)
                .build();
    }

    private static BillingProvider newApplandProvider() {
        final TypedMapSkuResolver skuResolver = new TypedMapSkuResolver();
        skuResolver.add(SKU_GAS, APPLAND_GAS, SkuType.CONSUMABLE);
        skuResolver.add(SKU_PREMIUM, APPLAND_PREMIUM, SkuType.ENTITLEMENT);
        skuResolver.add(SKU_SUBSCRIPTION, APPLAND_SUBSCRIPTION, SkuType.SUBSCRIPTION);

        return new ApplandBillingProvider.Builder(context)
                .setSkuResolver(skuResolver)
                .build();
    }

    private static BillingProvider newSlideMEProvider() {
        final TypedMapSkuResolver skuResolver = new TypedMapSkuResolver();
        skuResolver.add(SKU_GAS, SLIDEME_GAS, SkuType.CONSUMABLE);
        skuResolver.add(SKU_PREMIUM, SLIDEME_PREMIUM, SkuType.ENTITLEMENT);
        skuResolver.add(SKU_SUBSCRIPTION, SLIDEME_SUBSCRIPTION, SkuType.SUBSCRIPTION);

        return new SlideMEBillingProvider.Builder(context)
                .setSkuResolver(skuResolver)
                .build();
    }

    private static BillingProvider newOpenStoreProvider() {
        final TypedMapSkuResolver skuResolver = new TypedMapSkuResolver();
        //        skuResolver.add(SKU_GAS, YANDEX_SKU_GAS, SkuType.CONSUMABLE);
        //        skuResolver.add(SKU_PREMIUM, YANDEX_SKU_PREMIUM, SkuType.ENTITLEMENT);
        //        skuResolver.add(SKU_SUBSCRIPTION, YANDEX_SKU_SUBSCRIPTION, SkuType.SUBSCRIPTION);

        return new OpenStoreBillingProvider.Builder(context)
                .setSkuResolver(skuResolver)
                .build();
    }

    private TrivialBilling() {
        throw new UnsupportedOperationException();
    }

    public static void init(final Context context) {
        TrivialBilling.context = context.getApplicationContext();
        preferences = context.getSharedPreferences(NAME, Context.MODE_PRIVATE);

        if (preferences.getBoolean(KEY_FIRST_LAUNCH, true)) {
            preferences.edit().putBoolean(KEY_FIRST_LAUNCH, false).apply();
            setHelper(Helper.ACTIVITY);
            setProviders(Arrays.asList(Provider.values()));
            setAutoRecover(true);
            TrivialData.resetGas();
        }
    }

    public static Configuration getRelevantConfiguration(final Context context) {
        final Configuration.Builder builder = new Configuration.Builder();
        builder.setBillingListener(new TrivialBillingListener(context));
        for (final Provider provider : getProviders()) {
            builder.addBillingProvider(newProvider(provider));
        }
        builder.setAutoRecover(preferences.getBoolean(KEY_AUTO_RECOVER, false));
        return builder.build();
    }

    public static Helper getHelper() {
        return Helper.valueOf(preferences.getString(KEY_HELPER, null));
    }

    public static void setHelper(final Helper helper) {
        preferences.edit().putString(KEY_HELPER, helper.name()).apply();
    }

    public static Collection<Provider> getProviders() {
        try {
            final JSONObject jsonObject = new JSONObject(preferences.getString(KEY_PROVIDERS, ""));
            final JSONArray jsonArray = jsonObject.getJSONArray(KEY_PROVIDERS);
            final Collection<Provider> providers = new ArrayList<>(jsonArray.length());
            for (int i = 0; i < jsonArray.length(); i++) {
                providers.add(Provider.valueOf(jsonArray.getString(i)));
            }
            return providers;
        } catch (JSONException e) {
            return Collections.emptyList();
        }
    }

    public static void setProviders(final Iterable<Provider> providers) {
        final JSONObject jsonObject = new JSONObject();
        final JSONArray jsonArray = new JSONArray();
        for (final Provider provider : providers) {
            jsonArray.put(provider.name());
        }
        try {
            jsonObject.put(KEY_PROVIDERS, jsonArray);
            preferences.edit().putString(KEY_PROVIDERS, jsonObject.toString()).apply();
        } catch (JSONException ignore) { }
    }

    public static boolean isAutoRecover() {
        if (!preferences.contains(KEY_AUTO_RECOVER)) {
            throw new IllegalStateException();
        }
        return preferences.getBoolean(KEY_AUTO_RECOVER, false);
    }

    public static void setAutoRecover(final boolean autoRecover) {
        preferences.edit().putBoolean(KEY_AUTO_RECOVER, autoRecover).apply();
    }

    public static void updateSetup() {
        // clear all data if we change billing provider
        premium = false;
        subscription = false;
        DETAILS.clear();
    }

    private static Purchase getPurchase(final Map<Purchase, VerificationResult> inventory,
                                        final String sku) {
        for (final Map.Entry<Purchase, VerificationResult> entry : inventory.entrySet()) {
            final VerificationResult verificationResult = entry.getValue();
            if (verificationResult == VerificationResult.SUCCESS) {
                final Purchase purchase = entry.getKey();
                if (sku.equals(purchase.getSku())) {
                    return purchase;
                }
            }
        }
        return null;
    }

    public static void updateInventory(final InventoryResponse inventoryResponse) {
        final Map<Purchase, VerificationResult> inventory = inventoryResponse.getInventory();
        if (!inventoryResponse.isSuccessful()) {
            // Leave current values intact if request failed
            return;
        }
        if (getPurchase(inventory, SKU_PREMIUM) != null) {
            premium = true;
        }
        final Purchase purchase = getPurchase(inventory, SKU_SUBSCRIPTION);
        if (purchase != null && !purchase.isCanceled()) {
            subscription = true;
        }
    }

    public static void updatePurchase(final PurchaseResponse purchaseResponse) {
        final Purchase purchase = purchaseResponse.getPurchase();
        if (!purchaseResponse.isSuccessful()) {
            // Leave current values intact if request failed
            return;
        }
        //noinspection ConstantConditions
        final String sku = purchase.getSku();
        if (SKU_PREMIUM.equals(sku)) {
            premium = true;
        } else if (SKU_SUBSCRIPTION.equals(sku)) {
            subscription = !purchase.isCanceled();
        }
    }

    public static void updateSkuDetails(final SkuDetailsResponse skuDetailsResponse) {
        final Collection<SkuDetails> skusDetails = skuDetailsResponse.getSkusDetails();
        if (!skuDetailsResponse.isSuccessful()) {
            // Leave current values intact if request failed
            return;
        }
        for (final SkuDetails skuDetails : skusDetails) {
            final String sku = skuDetails.getSku();
            if (!skuDetails.isEmpty()) {
                DETAILS.put(sku, skuDetails);
            }
        }
    }

    public static boolean hasPremium() {
        return premium;
    }

    public static boolean hasValidSubscription() {
        return subscription;
    }

    public static SkuDetails getDetails(final String sku) {
        return DETAILS.get(sku);
    }
}
