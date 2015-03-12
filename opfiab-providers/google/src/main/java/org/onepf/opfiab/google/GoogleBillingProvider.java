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
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.json.JSONException;
import org.onepf.opfiab.BaseBillingProvider;
import org.onepf.opfiab.google.model.GooglePurchase;
import org.onepf.opfiab.google.model.GoogleSkuDetails;
import org.onepf.opfiab.google.model.ItemType;
import org.onepf.opfiab.model.BillingProviderInfo;
import org.onepf.opfiab.model.billing.Purchase;
import org.onepf.opfiab.model.billing.SkuDetails;
import org.onepf.opfiab.model.billing.SkuType;
import org.onepf.opfiab.model.event.billing.Status;
import org.onepf.opfiab.verification.PurchaseVerifier;
import org.onepf.opfutils.OPFLog;

import java.util.ArrayList;
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
        checkRequirements();
        helper = new GoogleBillingHelper(context);
    }

    protected void checkRequirements() {
        context.enforceCallingOrSelfPermission(PERMISSION_BILLING, null);
    }

    @NonNull
    private SkuDetails newSkuDetails(@NonNull final GoogleSkuDetails googleSkuDetails) {
        final String sku = googleSkuDetails.getProductId();
        return new SkuDetails.Builder(sku)
                .setType(googleSkuDetails.getItemType() == ItemType.SUBSCRIPTION
                                 ? SkuType.SUBSCRIPTION
                                 : skuResolver.resolveType(sku))
                .setTitle(googleSkuDetails.getTitle())
                .setDescription(googleSkuDetails.getDescription())
                .setPrice(googleSkuDetails.getPrice())
                .setProviderInfo(getInfo())
                .setOriginalJson(googleSkuDetails.getOriginalJson())
                .build();
    }

    @Nullable
    private Purchase newPurchase(@NonNull final String jsonPurchase) {
        try {
            final GooglePurchase googlePurchase = new GooglePurchase(jsonPurchase);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
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

    }

    @Override
    public void consume(@NonNull final Purchase purchase) {

    }

    @Override
    public void skuDetails(@NonNull final Set<String> skus) {
        final Bundle result = helper.getSkuDetails(skus);
        final Response response = Utils.getResponse(result);
        final List<String> jsonSkuDetails = Utils.getSkuDetails(result);
        if (response != Response.OK || jsonSkuDetails == null) {
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
                    OPFLog.e("Sku was not requested, yet it's returned by Google Play.");
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

    }

    @Override
    public void onActivityResult(@NonNull final Activity activity, final int requestCode,
                                 final int resultCode, @Nullable final Intent data) {

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
