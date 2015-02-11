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

package org.onepf.opfiab.amazon;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;

import com.amazon.device.iap.PurchasingService;
import com.amazon.device.iap.model.FulfillmentResult;
import com.amazon.device.iap.model.Product;
import com.amazon.device.iap.model.ProductDataResponse;
import com.amazon.device.iap.model.PurchaseUpdatesResponse;
import com.amazon.device.iap.model.Receipt;

import org.json.JSONException;
import org.onepf.opfiab.BaseBillingProvider;
import org.onepf.opfiab.model.BillingProviderInfo;
import org.onepf.opfiab.model.billing.Purchase;
import org.onepf.opfiab.model.billing.SkuDetails;
import org.onepf.opfiab.model.billing.SkuType;
import org.onepf.opfiab.sku.SkuResolver;
import org.onepf.opfiab.verification.PurchaseVerifier;
import org.onepf.opfutils.OPFLog;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import static org.onepf.opfiab.model.event.billing.Response.Status.ITEM_ALREADY_OWNED;
import static org.onepf.opfiab.model.event.billing.Response.Status.ITEM_UNAVAILABLE;
import static org.onepf.opfiab.model.event.billing.Response.Status.SUCCESS;
import static org.onepf.opfiab.model.event.billing.Response.Status.UNAUTHORISED;
import static org.onepf.opfiab.model.event.billing.Response.Status.UNKNOWN_ERROR;

public class AmazonBillingProvider extends BaseBillingProvider {

    private static final String NAME = "Amazon";
    private static final String PACKAGE_NAME = "com.amazon.venezia";

    static void post(@NonNull final Object event) {
        BaseBillingProvider.postEvent(event);
    }


    private final BillingProviderInfo info = new BillingProviderInfo(NAME, PACKAGE_NAME);
    @NonNull
    private final AmazonBillingController controller = new AmazonBillingController();

    protected AmazonBillingProvider(
            @NonNull final Context context,
            @NonNull final PurchaseVerifier purchaseVerifier,
            @NonNull final SkuResolver skuResolver) {
        super(context, purchaseVerifier, skuResolver);
        PurchasingService.registerListener(context, controller);
    }

    private SkuDetails newSkuDetails(@NonNull final Product product) {
        final SkuDetails.Builder builder = new SkuDetails.Builder(product.getSku());
        switch (product.getProductType()) {
            case CONSUMABLE:
                builder.setType(SkuType.CONSUMABLE);
                break;
            case ENTITLED:
                builder.setType(SkuType.ENTITLEMENT);
                break;
            case SUBSCRIPTION:
                builder.setType(SkuType.SUBSCRIPTION);
                break;
        }
        builder.setTitle(product.getTitle());
        builder.setDescription(product.getDescription());
        builder.setPrice(product.getPrice());
        builder.setIconUrl(product.getSmallIconUrl());
        try {
            builder.setJson(product.toJSON().toString());
        } catch (JSONException exception) {
            OPFLog.e("Failed to set original JSON for SkuDetails.", exception);
        }
        return builder.build();
    }

    private Purchase newPurchase(@NonNull final Receipt receipt) {
        final Purchase.Builder builder = new Purchase.Builder(receipt.getSku());
        switch (receipt.getProductType()) {
            case CONSUMABLE:
                builder.setType(SkuType.CONSUMABLE);
                break;
            case ENTITLED:
                builder.setType(SkuType.ENTITLEMENT);
                break;
            case SUBSCRIPTION:
                builder.setType(SkuType.SUBSCRIPTION);
                break;
        }
        builder.setToken(receipt.getReceiptId());
        builder.setCanceled(receipt.isCanceled());
        builder.setPurchaseTime(receipt.getPurchaseDate().getTime());
        builder.setJson(receipt.toJSON().toString());
        return builder.build();
    }

    @Override
    public void skuDetails(@NonNull final Set<String> skus) {
        PurchasingService.getProductData(skus);
    }

    @Override
    public void inventory() {
        PurchasingService.getPurchaseUpdates(true);
    }

    @Override
    public void purchase(@NonNull final Activity activity, @NonNull final String sku) {
        PurchasingService.purchase(sku);
    }

    @Override
    public void consume(@NonNull final Purchase purchase) {
        if (controller.getUserData() != null) {
            PurchasingService.notifyFulfillment(purchase.getSku(), FulfillmentResult.FULFILLED);
            postResponse(SUCCESS);
        } else {
            postResponse(UNAUTHORISED);
        }
    }

    public final void onEventAsync(@NonNull final ProductDataResponse productDataResponse) {
        switch (productDataResponse.getRequestStatus()) {
            case SUCCESSFUL:
                final List<SkuDetails> skusDetails = new ArrayList<>();
                final Collection<Product> products = productDataResponse.getProductData().values();
                for (final Product product : products) {
                    skusDetails.add(newSkuDetails(product));
                }
                for (final String sku : productDataResponse.getUnavailableSkus()) {
                    skusDetails.add(new SkuDetails(sku));
                }
                postResponse(SUCCESS, skusDetails);
                break;
            case FAILED:
                if (controller.getUserData() == null) {
                    postResponse(UNAUTHORISED);
                    break;
                }
            case NOT_SUPPORTED:
                OPFLog.e("Product data request failed: %s", productDataResponse);
                postResponse(UNKNOWN_ERROR);
                break;
        }
    }

    public final void onEventAsync(@NonNull final PurchaseUpdatesResponse purchaseUpdatesResponse) {
        switch (purchaseUpdatesResponse.getRequestStatus()) {
            case SUCCESSFUL:
                final List<Receipt> receipts = purchaseUpdatesResponse.getReceipts();
                final List<Purchase> purchases = new ArrayList<>(receipts.size());
                for (final Receipt receipt : receipts) {
                    purchases.add(newPurchase(receipt));
                }
                postResponse(SUCCESS, purchases);
                break;
            case FAILED:
                if (controller.getUserData() == null) {
                    postResponse(UNAUTHORISED);
                    break;
                }
            case NOT_SUPPORTED:
                OPFLog.e("Purchase updates request failed: %s", purchaseUpdatesResponse);
                postResponse(UNKNOWN_ERROR);
                break;
        }
    }

    public final void onEventAsync(
            @NonNull final com.amazon.device.iap.model.PurchaseResponse purchaseResponse) {
        switch (purchaseResponse.getRequestStatus()) {
            case SUCCESSFUL:
                final Purchase purchase = newPurchase(purchaseResponse.getReceipt());
                postResponse(SUCCESS, purchase);
                break;
            case INVALID_SKU:
                postResponse(ITEM_UNAVAILABLE);
                break;
            case ALREADY_PURCHASED:
                postResponse(ITEM_ALREADY_OWNED);
                break;
            case FAILED:
                if (controller.getUserData() == null) {
                    postResponse(UNAUTHORISED);
                    break;
                }
            case NOT_SUPPORTED:
                OPFLog.e("Purchase request failed: %s", purchaseResponse);
                postResponse(UNKNOWN_ERROR);
                break;
        }
    }

    @NonNull
    @Override
    public BillingProviderInfo getInfo() {
        return info;
    }

    @NonNull
    @Override
    public AmazonBillingController getController() {
        return controller;
    }


    public static class Builder extends BaseBillingProvider.Builder {

        public Builder(@NonNull final Context context) {
            super(context);
        }

        @Override
        public BaseBillingProvider build() {
            return new AmazonBillingProvider(context, purchaseVerifier, skuResolver);
        }

        @Override
        protected Builder setSkuResolver(
                @NonNull final SkuResolver skuResolver) {
            super.setSkuResolver(skuResolver);
            return this;
        }
    }
}
