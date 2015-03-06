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
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;

import com.amazon.device.iap.PurchasingService;
import com.amazon.device.iap.ResponseReceiver;
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
import org.onepf.opfiab.model.event.billing.Status;
import org.onepf.opfiab.sku.SkuResolver;
import org.onepf.opfiab.verification.PurchaseVerifier;
import org.onepf.opfutils.OPFLog;
import org.onepf.opfutils.OPFUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import static android.Manifest.permission.ACCESS_NETWORK_STATE;
import static org.onepf.opfiab.model.event.billing.Status.ITEM_ALREADY_OWNED;
import static org.onepf.opfiab.model.event.billing.Status.ITEM_UNAVAILABLE;
import static org.onepf.opfiab.model.event.billing.Status.SERVICE_UNAVAILABLE;
import static org.onepf.opfiab.model.event.billing.Status.SUCCESS;
import static org.onepf.opfiab.model.event.billing.Status.UNAUTHORISED;
import static org.onepf.opfiab.model.event.billing.Status.UNKNOWN_ERROR;

public class AmazonBillingProvider extends BaseBillingProvider {

    private static final String NAME = "Amazon";
    private static final String PACKAGE_NAME = "com.amazon.venezia";


    private final BillingProviderInfo info = new BillingProviderInfo(NAME, PACKAGE_NAME);
    @NonNull
    private final AmazonBillingHelper billingHelper = new AmazonBillingHelper();

    protected AmazonBillingProvider(
            @NonNull final Context context,
            @NonNull final PurchaseVerifier purchaseVerifier,
            @NonNull final SkuResolver skuResolver) {
        super(context, purchaseVerifier, skuResolver);
        checkRequirements();
        // Register Amazon callbacks handler
        PurchasingService.registerListener(context, billingHelper);
    }

    @SuppressFBWarnings({"EXS_EXCEPTION_SOFTENING_NO_CONSTRAINTS"})
    private void checkRequirements() {
        // Check if application is suited to use Amazon
        final PackageManager packageManager = context.getPackageManager();
        final ComponentName componentName = new ComponentName(context, ResponseReceiver.class);
        try {
            if (!packageManager.getReceiverInfo(componentName, 0).exported) {
                throw new IllegalStateException("Amazon receiver must be exported.");
            }
        } catch (PackageManager.NameNotFoundException exception) {
            throw new IllegalStateException(
                    "You must declare Amazon receiver to use Amazon billing provider.", exception);
        }
        context.enforceCallingOrSelfPermission(ACCESS_NETWORK_STATE, null);
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
            default:
                throw new IllegalStateException();
        }
        builder.setTitle(product.getTitle());
        builder.setDescription(product.getDescription());
        builder.setPrice(product.getPrice());
        builder.setIconUrl(product.getSmallIconUrl());
        builder.setProviderInfo(getInfo());
        try {
            builder.setOriginalJson(product.toJSON().toString());
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
            default:
                throw new IllegalStateException();
        }
        builder.setToken(receipt.getReceiptId());
        builder.setCanceled(receipt.isCanceled());
        builder.setPurchaseTime(receipt.getPurchaseDate().getTime());
        builder.setProviderInfo(getInfo());
        builder.setOriginalJson(receipt.toJSON().toString());
        return builder.build();
    }

    private Status handleFailure() {
        if (!OPFUtils.isConnected(context)) {
            return SERVICE_UNAVAILABLE;
        } else if (!isAuthorised()) {
            return UNAUTHORISED;
        }

        return UNKNOWN_ERROR;
    }

    public final void onEventAsync(@NonNull final ProductDataResponse productDataResponse) {
        switch (productDataResponse.getRequestStatus()) {
            case SUCCESSFUL:
                final Collection<SkuDetails> skusDetails = new ArrayList<>();
                final Collection<Product> products = productDataResponse.getProductData().values();
                for (final Product product : products) {
                    skusDetails.add(newSkuDetails(product));
                }
                for (final String sku : productDataResponse.getUnavailableSkus()) {
                    skusDetails.add(new SkuDetails(sku));
                }
                postSkuDetailsResponse(SUCCESS, skusDetails);
                break;
            case FAILED:
            case NOT_SUPPORTED:
                postSkuDetailsResponse(handleFailure(), null);
                OPFLog.e("Product data request failed: %s", productDataResponse);
                break;
            default:
                throw new IllegalStateException();
        }
    }

    public final void onEventAsync(@NonNull final PurchaseUpdatesResponse purchaseUpdatesResponse) {
        switch (purchaseUpdatesResponse.getRequestStatus()) {
            case SUCCESSFUL:
                final List<Receipt> receipts = purchaseUpdatesResponse.getReceipts();
                final Collection<Purchase> purchases = new ArrayList<>(receipts.size());
                for (final Receipt receipt : receipts) {
                    purchases.add(newPurchase(receipt));
                }
                final boolean hasMore = purchaseUpdatesResponse.hasMore();
                postInventoryResponse(SUCCESS, purchases, hasMore);
                break;
            case FAILED:
            case NOT_SUPPORTED:
                postInventoryResponse(handleFailure(), null, false);
                OPFLog.e("Purchase updates request failed: %s", purchaseUpdatesResponse);
                break;
            default:
                throw new IllegalStateException();
        }
    }

    public final void onEventAsync(
            @NonNull final com.amazon.device.iap.model.PurchaseResponse purchaseResponse) {
        switch (purchaseResponse.getRequestStatus()) {
            case SUCCESSFUL:
                final Purchase purchase = newPurchase(purchaseResponse.getReceipt());
                postPurchaseResponse(SUCCESS, purchase);
                break;
            case INVALID_SKU:
                postPurchaseResponse(ITEM_UNAVAILABLE, null);
                break;
            case ALREADY_PURCHASED:
                postPurchaseResponse(ITEM_ALREADY_OWNED, null);
                break;
            case FAILED:
            case NOT_SUPPORTED:
                postPurchaseResponse(handleFailure(), null);
                OPFLog.e("Purchase request failed: %s", purchaseResponse);
                break;
            default:
                throw new IllegalStateException();
        }
    }

    @Override
    public boolean isAvailable() {
        return PurchasingService.IS_SANDBOX_MODE || super.isAvailable();
    }

    @Override
    public boolean isAuthorised() {
        return billingHelper.getUserData() != null;
    }

    @Override
    public void skuDetails(@NonNull final Set<String> skus) {
        PurchasingService.getProductData(skus);
    }

    @Override
    public void inventory(final boolean startOver) {
        PurchasingService.getPurchaseUpdates(startOver);
    }

    @Override
    public void purchase(@NonNull final Activity activity, @NonNull final String sku) {
        PurchasingService.purchase(sku);
    }

    @Override
    public void consume(@NonNull final Purchase purchase) {
        PurchasingService.notifyFulfillment(purchase.getSku(), FulfillmentResult.FULFILLED);
        postConsumeResponse(SUCCESS, purchase);
    }

    @NonNull
    @Override
    public BillingProviderInfo getInfo() {
        return info;
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
