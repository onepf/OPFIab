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

package org.onepf.opfiab.google;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import org.json.JSONException;
import org.onepf.opfiab.BaseBillingProvider;
import org.onepf.opfiab.google.model.GoogleModel;
import org.onepf.opfiab.google.model.GooglePurchase;
import org.onepf.opfiab.google.model.GoogleSkuDetails;
import org.onepf.opfiab.google.model.ItemType;
import org.onepf.opfiab.google.model.PurchaseState;
import org.onepf.opfiab.google.model.SignedPurchase;
import org.onepf.opfiab.model.BillingProviderInfo;
import org.onepf.opfiab.model.billing.Purchase;
import org.onepf.opfiab.model.billing.SkuDetails;
import org.onepf.opfiab.model.billing.SkuType;
import org.onepf.opfiab.model.event.billing.Status;
import org.onepf.opfiab.verification.PurchaseVerifier;
import org.onepf.opfutils.OPFLog;
import org.onepf.opfutils.OPFUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class GoogleBillingProvider
        extends BaseBillingProvider<GoogleSkuResolver, PurchaseVerifier> {

    protected static final String NAME = "Google";
    protected static final String PACKAGE_NAME = "com.google.play";
    protected static final String PERMISSION_BILLING = "com.android.vending.BILLING";

    static final BillingProviderInfo INFO = new BillingProviderInfo(NAME, PACKAGE_NAME);


    protected final GoogleBillingHelper helper;

    protected GoogleBillingProvider(
            @NonNull final Context context,
            @NonNull final GoogleSkuResolver skuResolver,
            @NonNull final PurchaseVerifier purchaseVerifier) {
        super(context, skuResolver, purchaseVerifier);
        helper = new GoogleBillingHelper(context);
    }

    @NonNull
    private SkuType skuType(@NonNull final GoogleModel googleModel) {
        final String sku = googleModel.getProductId();
        return googleModel.getItemType() == ItemType.SUBSCRIPTION
                ? SkuType.SUBSCRIPTION
                : skuResolver.resolveType(sku);
    }

    @NonNull
    private SkuDetails newSkuDetails(@NonNull final GoogleSkuDetails googleSkuDetails) {
        final String sku = googleSkuDetails.getProductId();
        return new SkuDetails.Builder(sku)
                .setType(skuType(googleSkuDetails))
                .setProviderInfo(getInfo())
                .setOriginalJson(googleSkuDetails.getOriginalJson())
                .setPrice(googleSkuDetails.getPrice())
                .setTitle(googleSkuDetails.getTitle())
                .setDescription(googleSkuDetails.getDescription())
                .build();
    }

    @NonNull
    private Purchase newPurchase(@NonNull final GooglePurchase googlePurchase) {
        final String sku = googlePurchase.getProductId();
        return new Purchase.Builder(sku)
                .setType(skuType(googlePurchase))
                .setProviderInfo(getInfo())
                .setOriginalJson(googlePurchase.getOriginalJson())
                .setToken(googlePurchase.getPurchaseToken())
                .setPurchaseTime(googlePurchase.getPurchaseTime())
                .setCanceled(googlePurchase.getPurchaseState() == PurchaseState.CANCELED)
                .build();
    }

    private Status handleFailure(@Nullable final Response response) {
        if (response == null) {
            return Status.UNKNOWN_ERROR;
        }
        switch (response) {
            case USER_CANCELED:
                return Status.USER_CANCELED;
            case SERVICE_UNAVAILABLE:
                return Status.SERVICE_UNAVAILABLE;
            case ITEM_UNAVAILABLE:
                return Status.ITEM_UNAVAILABLE;
            case ITEM_ALREADY_OWNED:
                return Status.ITEM_ALREADY_OWNED;
            default:
                return Status.UNKNOWN_ERROR;
        }
    }

    @Override
    protected void checkRequirements() {
        context.enforceCallingOrSelfPermission(PERMISSION_BILLING, null);
    }

    @Override
    public boolean isAvailable() {
        final Response response = helper.isBillingSupported();
        OPFLog.d("Check if billing supported: %s", response);
        return response == Response.OK;
    }

    @NonNull
    @Override
    public BillingProviderInfo getInfo() {
        return INFO;
    }

    @Override
    public void purchase(@NonNull final Activity activity, @NonNull final String sku) {
        final Bundle details = helper.getSkuDetails(Arrays.asList(sku));
        final Response detailsResponse = GoogleUtils.getResponse(details);
        final List<String> jsonSkuDetails = GoogleUtils.getSkuDetails(details);
        if (detailsResponse != Response.OK || jsonSkuDetails == null || jsonSkuDetails.isEmpty()) {
            OPFLog.e("Failed to retrieve sku details.");
            postPurchaseResponse(handleFailure(detailsResponse), null);
            return;
        }

        final GoogleSkuDetails googleSkuDetails;
        try {
            googleSkuDetails = new GoogleSkuDetails(jsonSkuDetails.get(0));
        } catch (JSONException exception) {
            OPFLog.e("Failed to parse sku details.", exception);
            postPurchaseResponse(Status.UNKNOWN_ERROR, null);
            return;
        }

        final ItemType itemType = googleSkuDetails.getItemType();
        final Bundle result = helper.getBuyIntent(sku, itemType);
        final Response response = GoogleUtils.getResponse(result);
        final PendingIntent intent = GoogleUtils.getBuyIntent(result);
        if (response != Response.OK || intent == null) {
            OPFLog.e("Failed to retrieve buy intent.");
            postPurchaseResponse(handleFailure(response), null);
            return;
        }

        final IntentSender sender = intent.getIntentSender();
        try {
            activity.startIntentSenderForResult(sender, requestCode, new Intent(), 0, 0, 0);
        } catch (IntentSender.SendIntentException exception) {
            OPFLog.e("Failed to send buy intent.", exception);
            postPurchaseResponse(Status.UNKNOWN_ERROR, null);
        }
    }

    @Override
    public void consume(@NonNull final Purchase purchase) {
        final String token = purchase.getToken();
        if (TextUtils.isEmpty(token)) {
            OPFLog.e("Purchase toke in empty.");
            postConsumeResponse(Status.ITEM_UNAVAILABLE, purchase);
            return;
        }

        final Response response = helper.consumePurchase(token);
        if (response != Response.OK) {
            OPFLog.e("Consume failed.");
            postConsumeResponse(handleFailure(response), purchase);
            return;
        }

        postConsumeResponse(Status.SUCCESS, purchase);
    }

    @Override
    public void skuDetails(@NonNull final Set<String> skus) {
        final Bundle result = helper.getSkuDetails(skus);
        final Response response = GoogleUtils.getResponse(result);
        final Collection<String> jsonSkuDetails = GoogleUtils.getSkuDetails(result);
        if (response != Response.OK || jsonSkuDetails == null) {
            OPFLog.e("Failed to retrieve sku details.");
            postSkuDetailsResponse(handleFailure(response), null);
            return;
        }

        final Collection<String> unresolvedSkus = new LinkedList<>(skus);
        final Collection<SkuDetails> skusDetails = new ArrayList<>();
        for (final String jsonSku : jsonSkuDetails) {
            try {
                final GoogleSkuDetails googleSkuDetails = new GoogleSkuDetails(jsonSku);
                final SkuDetails skuDetails = newSkuDetails(googleSkuDetails);
                skusDetails.add(skuDetails);
                if (!unresolvedSkus.remove(skuDetails.getSku())) {
                    OPFLog.e("Sku was not requested, yet it's returned.");
                }
            } catch (JSONException exception) {
                OPFLog.e("Failed to parse sku details.", exception);
            }
        }
        for (final String sku : unresolvedSkus) {
            skusDetails.add(new SkuDetails(sku));
        }
        postSkuDetailsResponse(Status.SUCCESS, skusDetails);
    }

    @Override
    public void inventory(final boolean startOver) {
        final Bundle result = helper.getPurchases(startOver);
        final Response response = GoogleUtils.getResponse(result);
        final Collection<String> itemList = GoogleUtils.getItemList(result);
        final List<String> dataList = GoogleUtils.getDataList(result);
        final List<String> signatureList = GoogleUtils.getSignatureList(result);
        if (response != Response.OK || itemList == null || dataList == null || signatureList == null
                || (itemList.size() != dataList.size() || dataList.size() != signatureList.size())) {
            OPFLog.e("Failed to retrieve purchase data.");
            postInventoryResponse(handleFailure(response), null, false);
            return;
        }

        final int size = dataList.size();
        final Collection<Purchase> inventory = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            final String data = dataList.get(i);
            try {
                final GooglePurchase googlePurchase = new GooglePurchase(data);
                final Purchase purchase = newPurchase(googlePurchase);
                final String signature = signatureList.get(i);
                final SignedPurchase signedPurchase = new SignedPurchase(purchase, signature);
                inventory.add(signedPurchase);
            } catch (JSONException exception) {
                OPFLog.e("Failed to parse purchase data.", exception);
            }
        }
        final String token = GoogleUtils.getContinuationToken(result);
        final boolean hasMore = !TextUtils.isEmpty(token);
        postInventoryResponse(Status.SUCCESS, inventory, hasMore);
    }

    @Override
    public void onActivityResult(@NonNull final Activity activity, final int requestCode,
                                 final int resultCode, @Nullable final Intent data) {
        final String purchaseData = GoogleUtils.getPurchaseData(data);
        final String signature = GoogleUtils.getSignature(data);
        if (resultCode != Activity.RESULT_OK || purchaseData == null || signature == null) {
            OPFLog.e("Failed to handle activity result. Code:%s, Data:%s",
                     resultCode, OPFUtils.toString(data));
            postPurchaseResponse(Status.UNKNOWN_ERROR, null);
            return;
        }

        final GooglePurchase googlePurchase;
        try {
            googlePurchase = new GooglePurchase(purchaseData);
        } catch (JSONException exception) {
            OPFLog.e("Failed to parse purchase data.", exception);
            postPurchaseResponse(Status.UNKNOWN_ERROR, null);
            return;
        }

        final Purchase purchase = newPurchase(googlePurchase);
        final SignedPurchase signedPurchase = new SignedPurchase(purchase, signature);
        postPurchaseResponse(Status.SUCCESS, signedPurchase);
    }


    public static class Builder
            extends BaseBillingProvider.Builder<GoogleSkuResolver, PurchaseVerifier> {

        public Builder(@NonNull final Context context) {
            super(context, GoogleSkuResolver.STUB, PurchaseVerifier.STUB);
        }

        @Override
        public GoogleBillingProvider build() {
            return new GoogleBillingProvider(context, skuResolver, purchaseVerifier);
        }

        @Override
        public Builder setSkuResolver(@NonNull final GoogleSkuResolver skuResolver) {
            return (Builder) super.setSkuResolver(skuResolver);
        }

        @Override
        public Builder setPurchaseVerifier(@NonNull final PurchaseVerifier purchaseVerifier) {
            return (Builder) super.setPurchaseVerifier(purchaseVerifier);
        }
    }
}
