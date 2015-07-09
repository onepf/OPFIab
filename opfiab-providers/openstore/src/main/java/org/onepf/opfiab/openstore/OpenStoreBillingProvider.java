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

package org.onepf.opfiab.openstore;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import org.onepf.opfiab.billing.BaseBillingProvider;
import org.onepf.opfiab.billing.BaseBillingProviderBuilder;
import org.onepf.opfiab.billing.Compatibility;
import org.onepf.opfiab.model.billing.Purchase;
import org.onepf.opfiab.model.billing.SkuDetails;
import org.onepf.opfiab.model.billing.SkuType;
import org.onepf.opfiab.model.event.android.ActivityResult;
import org.onepf.opfiab.model.event.billing.ConsumeRequest;
import org.onepf.opfiab.model.event.billing.ConsumeResponse;
import org.onepf.opfiab.model.event.billing.InventoryRequest;
import org.onepf.opfiab.model.event.billing.InventoryResponse;
import org.onepf.opfiab.model.event.billing.PurchaseRequest;
import org.onepf.opfiab.model.event.billing.PurchaseResponse;
import org.onepf.opfiab.model.event.billing.SkuDetailsRequest;
import org.onepf.opfiab.model.event.billing.SkuDetailsResponse;
import org.onepf.opfiab.model.event.billing.Status;
import org.onepf.opfiab.openstore.model.ItemType;
import org.onepf.opfiab.openstore.model.OpenPurchase;
import org.onepf.opfiab.openstore.model.OpenSkuDetails;
import org.onepf.opfiab.sku.TypedSkuResolver;
import org.onepf.opfiab.util.ActivityForResultLauncher;
import org.onepf.opfiab.verification.PurchaseVerifier;
import org.onepf.opfutils.OPFChecks;
import org.onepf.opfutils.OPFLog;
import org.onepf.opfutils.OPFPreferences;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;

@SuppressWarnings({"PMD.GodClass", "PMD.EmptyMethodInAbstractClassShouldBeAbstract", "PMD.NPathComplexity"})
public class OpenStoreBillingProvider extends BaseBillingProvider<TypedSkuResolver,
        PurchaseVerifier> {

    protected static final String KEY_TOKEN = "continuation_token";

    @Nullable
    protected final OpenStoreIntentMaker intentMaker;
    protected final OpenStoreBillingHelper helper;
    @Nullable
    private String name;

    protected OpenStoreBillingProvider(@NonNull final Context context,
                                       @NonNull final TypedSkuResolver skuResolver,
                                       @NonNull final PurchaseVerifier purchaseVerifier,
                                       @Nullable final OpenStoreIntentMaker intentMaker) {
        super(context, skuResolver, purchaseVerifier);
        this.intentMaker = intentMaker;
        this.helper = new OpenStoreBillingHelper(context, intentMaker);
    }

    @NonNull
    protected final OPFPreferences getPreferences() {
        // Store name can't be null. If store is unavailable this method shouldn't be used.
        return new OPFPreferences(context, getName());
    }

    /**
     * Picks proper response status for supplied response.
     *
     * @param response Response to pick status for.
     *
     * @return Billing response status most fitting supplied response. Can't be null.
     */
    @NonNull
    protected Status getStatus(@Nullable final Response response) {
        if (response == null) {
            return Status.UNKNOWN_ERROR;
        }
        switch (response) {
            case OK:
                return Status.SUCCESS;
            case USER_CANCELED:
                return Status.USER_CANCELED;
            case SERVICE_UNAVAILABLE:
                return Status.SERVICE_UNAVAILABLE;
            case ITEM_UNAVAILABLE:
                return Status.ITEM_UNAVAILABLE;
            case ITEM_ALREADY_OWNED:
                return Status.ITEM_ALREADY_OWNED;
            case BILLING_UNAVAILABLE:
                return Status.UNAUTHORISED;
            default:
                return Status.UNKNOWN_ERROR;
        }
    }

    @Override
    public String toString() {
        return intentMaker != null ? intentMaker.getProviderName() : getClass().getSimpleName();
    }

    @NonNull
    @Override
    public String getName() {
        if (name == null) {
            throw new IllegalStateException();
        }
        return name;
    }

    @Override
    public void checkManifest() {
        // Nothing to check
    }

    @Override
    public boolean isAvailable() {
        OPFChecks.checkThread(false);
        final String appstoreName = helper.getAppstoreName();
        if (appstoreName == null) {
            return false;
        }
        this.name = appstoreName;
        return helper.isBillingAvailable();
    }

    @NonNull
    @Override
    public Compatibility checkCompatibility() {
        if (helper.isPackageInstaller()) {
            return Compatibility.PREFERRED;
        }
        return Compatibility.COMPATIBLE;
    }

    @Override
    public boolean skuTypeSupported(@NonNull final SkuType skuType) {
        OPFChecks.checkThread(false);
        final ItemType itemType = ItemType.fromSkuType(skuType);
        return itemType != null && helper.isBillingSupported(itemType) == Response.OK;
    }

    @Nullable
    @Override
    public Intent getStorePageIntent() {
        OPFChecks.checkThread(false);
        return helper.getProductPageIntent();
    }

    @Nullable
    @Override
    public Intent getRateIntent() {
        OPFChecks.checkThread(false);
        return helper.getProductPageIntent();
    }

    @Override
    protected void skuDetails(@NonNull final SkuDetailsRequest request) {
        final Map<ItemType, Collection<String>> typeSkuMap = new HashMap<>();
        final Set<String> skus = request.getSkus();
        for (final String sku : skus) {
            final SkuType skuType = skuResolver.resolveType(sku);
            final ItemType itemType = ItemType.fromSkuType(skuType);
            if (itemType == null) {
                OPFLog.e("Unknown SKU type: " + sku);
                continue;
            }
            final Collection<String> typeSkus;
            if (!typeSkuMap.containsKey(itemType)) {
                typeSkus = new ArrayList<>();
                typeSkuMap.put(itemType, typeSkus);
            } else {
                typeSkus = typeSkuMap.get(itemType);
            }
            typeSkus.add(sku);
        }
        final Bundle result = helper.getSkuDetails(typeSkuMap);
        final Response response = OpenStoreUtils.getResponse(result);
        if (response != Response.OK) {
            postEmptyResponse(request, getStatus(response));
            return;
        }
        final Collection<OpenSkuDetails> openSkusDetails = OpenStoreUtils.getSkusDetails(result);
        if (openSkusDetails == null) {
            postEmptyResponse(request, Status.UNKNOWN_ERROR);
            return;
        }
        final Collection<SkuDetails> skusDetails = new ArrayList<>();
        final Collection<String> unresolvedSkus = new HashSet<>(skus);
        for (final OpenSkuDetails openSkuDetails : openSkusDetails) {
            final String sku = openSkuDetails.getProductId();
            final SkuType skuType = skuResolver.resolveType(sku);
            skusDetails.add(OpenStoreUtils.convertSkuDetails(openSkuDetails, getName(), skuType));
            unresolvedSkus.remove(sku);
        }
        for (final String unresolvedSku : unresolvedSkus) {
            skusDetails.add(new SkuDetails(unresolvedSku));
        }
        postResponse(new SkuDetailsResponse(Status.SUCCESS, getName(), skusDetails));
    }

    private String getTokenKey(@NonNull final ItemType itemType) {
        return KEY_TOKEN + "." + itemType.toString();
    }

    @Override
    protected void inventory(@NonNull final InventoryRequest request) {
        final boolean startOver = request.startOver();
        final OPFPreferences preferences = getPreferences();
        final Map<ItemType, Bundle> resultMap = new HashMap<>();
        for (final SkuType skuType : SkuType.values()) {
            final ItemType itemType = ItemType.fromSkuType(skuType);
            if (itemType == null || resultMap.containsKey(itemType)
                    || !skuTypeSupported(skuType)) {
                continue;
            }
            final String token = startOver ? null : preferences.getString(getTokenKey(itemType));
            final Bundle result = helper.getPurchases(itemType, token);
            final Response response = OpenStoreUtils.getResponse(result);
            if (response != Response.OK) {
                postEmptyResponse(request, getStatus(response));
                return;
            }
            resultMap.put(itemType, result);
        }

        final Collection<Purchase> inventory = new ArrayList<>();
        boolean hasMore = false;
        for (final Map.Entry<ItemType, Bundle> entry : resultMap.entrySet()) {
            final Bundle result = entry.getValue();
            final Collection<OpenPurchase> purchases = OpenStoreUtils.getPurchases(result);
            final Collection<String> signatures = OpenStoreUtils.getSignaturesList(result);
            if (purchases == null || signatures == null) {
                OPFLog.e("Invalid inventory data. Purchases: %s. Signatures: %s."
                        , purchases, signatures);
                postEmptyResponse(request, Status.UNKNOWN_ERROR);
                return;
            }
            final Iterator<OpenPurchase> purchaseIterator = purchases.iterator();
            final Iterator<String> signatureIterator = signatures.iterator();
            while (purchaseIterator.hasNext()) {
                final OpenPurchase purchase = purchaseIterator.next();
                final String signature = signatureIterator.next();
                final SkuType skuType = skuResolver.resolveType(purchase.getProductId());
                inventory.add(
                        OpenStoreUtils.convertPurchase(purchase, getName(), skuType, signature));
            }

            final String continuationToken = OpenStoreUtils.getContinuationToken(result);
            final String tokenKey = getTokenKey(entry.getKey());
            if (TextUtils.isEmpty(continuationToken)) {
                preferences.remove(tokenKey);
            } else {
                preferences.put(tokenKey, continuationToken);
                hasMore = true;
            }
        }
        postResponse(new InventoryResponse(Status.SUCCESS, getName(), inventory, hasMore));
    }

    @Override
    protected void consume(@NonNull final ConsumeRequest request) {
        final Purchase purchase = request.getPurchase();
        final String token = purchase.getToken();
        //noinspection ConstantConditions
        final Response response = helper.consumePurchase(token);
        if (response != Response.OK) {
            postEmptyResponse(request, getStatus(response));
            return;
        }

        postResponse(new ConsumeResponse(Status.SUCCESS, getName(), purchase));
    }

    @SuppressWarnings("PMD.PrematureDeclaration")
    @Override
    protected void purchase(@NonNull final PurchaseRequest request) {
        final String sku = request.getSku();
        final SkuType skuType = skuResolver.resolveType(sku);
        //noinspection ConstantConditions
        final ItemType itemType = ItemType.fromSkuType(skuType);
        if (itemType == null) {
            OPFLog.e("Unknown SKU type: " + sku);
            postEmptyResponse(request, Status.ITEM_UNAVAILABLE);
            return;
        }
        final Bundle intentBundle = helper.getBuyIntent(sku, itemType);
        final Response intentResponse = OpenStoreUtils.getResponse(intentBundle);
        if (intentResponse != Response.OK) {
            postEmptyResponse(request, getStatus(intentResponse));
            return;
        }

        final PendingIntent intent = OpenStoreUtils.getPurchaseIntent(intentBundle);
        if (intent == null) {
            OPFLog.e("No purchase intent.");
            postEmptyResponse(request, Status.UNKNOWN_ERROR);
            return;
        }

        final ActivityResult activityResult = requestActivityResult(request,
                new ActivityForResultLauncher(DEFAULT_REQUEST_CODE) {
                    @Override
                    public void onStartForResult(@NonNull final Activity activity)
                            throws IntentSender.SendIntentException {
                        activity.startIntentSenderForResult(intent.getIntentSender(),
                                DEFAULT_REQUEST_CODE, new Intent(), 0, 0, 0);
                    }
                });
        if (activityResult == null) {
            OPFLog.e("No activity result.");
            postEmptyResponse(request, Status.UNKNOWN_ERROR);
            return;
        }
        final int resultCode = activityResult.getResultCode();
        final Intent data;
        if (resultCode != RESULT_OK || (data = activityResult.getData()) == null) {
            final Status status = resultCode == RESULT_CANCELED
                    ? Status.USER_CANCELED : Status.UNKNOWN_ERROR;
            postEmptyResponse(request, status);
            return;
        }

        final Bundle result = data.getExtras();
        final Response response = OpenStoreUtils.getResponse(result);
        if (response != Response.OK) {
            postEmptyResponse(request, getStatus(response));
            return;
        }
        final OpenPurchase openPurchase = OpenStoreUtils.getPurchase(result);
        if (openPurchase == null) {
            postEmptyResponse(request, Status.UNKNOWN_ERROR);
            return;
        }
        final String signature = OpenStoreUtils.getSignature(result);
        final Purchase purchase = OpenStoreUtils
                .convertPurchase(openPurchase, getName(), skuType, signature);
        postResponse(new PurchaseResponse(Status.SUCCESS, getName(), purchase));
    }


    protected abstract static class OpenStoreBuilder<B extends OpenStoreBuilder>
            extends BaseBillingProviderBuilder<B, TypedSkuResolver, PurchaseVerifier> {

        @Nullable
        protected OpenStoreIntentMaker intentMaker;

        public OpenStoreBuilder(@NonNull final Context context) {
            super(context);
        }

        protected OpenStoreBuilder setIntentMaker(
                @NonNull final OpenStoreIntentMaker intentMaker) {
            this.intentMaker = intentMaker;
            return this;
        }
    }

    public static class Builder extends OpenStoreBuilder<Builder> {

        public Builder(@NonNull final Context context) {
            super(context);
        }

        @Override
        public OpenStoreBillingProvider build() {
            if (skuResolver == null) {
                throw new IllegalStateException();
            }
            return new OpenStoreBillingProvider(context, skuResolver,
                    purchaseVerifier == null ? PurchaseVerifier.DEFAULT : purchaseVerifier,
                    intentMaker);
        }
    }
}
