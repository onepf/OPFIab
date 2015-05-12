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
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.amazon.device.iap.PurchasingService;
import com.amazon.device.iap.ResponseReceiver;
import com.amazon.device.iap.model.FulfillmentResult;
import com.amazon.device.iap.model.Product;
import com.amazon.device.iap.model.ProductDataResponse;
import com.amazon.device.iap.model.PurchaseUpdatesResponse;
import com.amazon.device.iap.model.Receipt;

import org.json.JSONException;
import org.onepf.opfiab.billing.BaseBillingProvider;
import org.onepf.opfiab.billing.BillingProvider;
import org.onepf.opfiab.model.BillingProviderInfo;
import org.onepf.opfiab.model.billing.Purchase;
import org.onepf.opfiab.model.billing.SkuDetails;
import org.onepf.opfiab.model.billing.SkuType;
import org.onepf.opfiab.model.event.billing.BillingRequest;
import org.onepf.opfiab.model.event.billing.Status;
import org.onepf.opfiab.sku.SkuResolver;
import org.onepf.opfiab.verification.PurchaseVerifier;
import org.onepf.opfutils.OPFChecks;
import org.onepf.opfutils.OPFLog;
import org.onepf.opfutils.OPFUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
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

/**
 * This {@link BillingProvider} implementation adds support of
 * <a href="http://www.amazon.com/mobile-apps/b?node=2350149011">Amazon Appstore</a>
 */
@SuppressWarnings("PMD.GodClass")
public class AmazonBillingProvider extends BaseBillingProvider<SkuResolver, PurchaseVerifier> {

    protected static final String NAME = "Amazon";
    protected static final String INSTALLER = "com.amazon.venezia";
    protected static final Collection<String> PACKAGES = Collections.unmodifiableList(
            Arrays.asList("com.amazon.venezia", "com.amazon.mShop.android"));

    public static final BillingProviderInfo INFO = new BillingProviderInfo(NAME, INSTALLER);

    /**
     * Helper object handles all Amazon SDK related calls.
     */
    protected static AmazonBillingHelper billingHelper;


    @SuppressWarnings("AssignmentToStaticFieldFromInstanceMethod")
    protected AmazonBillingProvider(
            @NonNull final Context context,
            @NonNull final SkuResolver skuResolver,
            @NonNull final PurchaseVerifier purchaseVerifier) {
        super(context, skuResolver, purchaseVerifier);
        if (billingHelper == null) {
            billingHelper = new AmazonBillingHelper();
            // Register Amazon callbacks handler, it's never unregistered.
            PurchasingService.registerListener(context, billingHelper);
        }
    }

    /**
     * Transforms Amazon product into library SKU details model.
     *
     * @param product Amazon product to transform.
     *
     * @return Newly constructed SkuDetails object.
     */
    protected SkuDetails newSkuDetails(@NonNull final Product product) {
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

    /**
     * Transforms Amazon receipt into library purchase model.
     *
     * @param receipt Amazon receipt to transform.
     *
     * @return Newly constructed purchase object.
     */
    protected Purchase newPurchase(@NonNull final Receipt receipt) {
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

    /**
     * Tries to guess appropriate error code.
     *
     * @return Most suitable status.
     */
    protected Status handleFailure() {
        // Unfortunately Amazon doesn't report a reason for error
        if (!PurchasingService.IS_SANDBOX_MODE && !OPFUtils.isConnected(context)) {
            return SERVICE_UNAVAILABLE;
        }

        return UNKNOWN_ERROR;
    }

    /**
     * Handles sku details response from Amazon.
     *
     * @param productDataResponse Response to handle.
     */
    public void onEventAsync(@NonNull final ProductDataResponse productDataResponse) {
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

    /**
     * Handles inventory response from Amazon.
     *
     * @param purchaseUpdatesResponse Response to handle.
     */
    public void onEventAsync(@NonNull final PurchaseUpdatesResponse purchaseUpdatesResponse) {
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

    /**
     * Handles purchase response from Amazon.
     *
     * @param purchaseResponse Response to handle.`
     */
    public void onEventAsync(
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

    @SuppressFBWarnings({"EXS_EXCEPTION_SOFTENING_NO_CONSTRAINTS"})
    @Override
    public void checkManifest() {
        //TODO OPFCheck.checkReceiver
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
        OPFChecks.checkPermission(context, ACCESS_NETWORK_STATE);
    }

    @Override
    public boolean isAvailable() {
        for (final String packageName : PACKAGES) {
            if (OPFUtils.isInstalled(context, packageName)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isAuthorised() {
        return billingHelper.getUserData() != null;
    }

    @Override
    protected void handleRequest(@NonNull final BillingRequest billingRequest) {
        if (!isAuthorised()) {
            postEmptyResponse(billingRequest, UNAUTHORISED);
        } else {
            super.handleRequest(billingRequest);
        }
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
    public void purchase(@Nullable final Activity activity, @NonNull final String sku) {
        PurchasingService.purchase(sku);
    }

    @Override
    public void consume(@NonNull final Purchase purchase) {
        final String token = purchase.getToken();
        if (!TextUtils.isEmpty(token)) {
            PurchasingService.notifyFulfillment(token, FulfillmentResult.FULFILLED);
            postConsumeResponse(SUCCESS, purchase);
        } else {
            postConsumeResponse(ITEM_UNAVAILABLE, purchase);
        }
    }

    @NonNull
    @Override
    public BillingProviderInfo getInfo() {
        return INFO;
    }

    public static class Builder extends BaseBillingProvider.Builder<SkuResolver, PurchaseVerifier> {

        public Builder(@NonNull final Context context) {
            super(context, SkuResolver.DEFAULT, PurchaseVerifier.DEFAULT);
        }

        @Override
        public BaseBillingProvider build() {
            return new AmazonBillingProvider(context, skuResolver, purchaseVerifier);
        }

        @Override
        public Builder setSkuResolver(@NonNull final SkuResolver skuResolver) {
            return (Builder) super.setSkuResolver(skuResolver);
        }

        @Override
        public Builder setPurchaseVerifier(@NonNull final PurchaseVerifier purchaseVerifier) {
            return (Builder) super.setPurchaseVerifier(purchaseVerifier);
        }
    }
}
