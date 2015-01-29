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
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.amazon.device.iap.model.ProductDataResponse;
import com.amazon.device.iap.model.PurchaseResponse;
import com.amazon.device.iap.model.PurchaseUpdatesResponse;
import com.amazon.device.iap.model.Receipt;
import com.amazon.device.iap.model.UserData;

import org.onepf.opfiab.BaseBillingProvider;
import org.onepf.opfiab.OPFIabUtils;
import org.onepf.opfiab.billing.BillingController;
import org.onepf.opfiab.model.BillingProviderInfo;
import org.onepf.opfiab.model.billing.ConsumableDetails;
import org.onepf.opfiab.model.billing.ConsumablePurchase;
import org.onepf.opfiab.model.billing.Inventory;
import org.onepf.opfiab.model.billing.Purchase;
import org.onepf.opfiab.model.billing.SkuDetails;
import org.onepf.opfiab.model.billing.SkusDetails;
import org.onepf.opfiab.sku.SkuResolver;
import org.onepf.opfiab.verification.PurchaseVerifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import static org.onepf.opfiab.model.event.response.Response.Status.ITEM_ALREADY_OWNED;
import static org.onepf.opfiab.model.event.response.Response.Status.ITEM_UNAVAILABLE;
import static org.onepf.opfiab.model.event.response.Response.Status.SUCCESS;
import static org.onepf.opfiab.model.event.response.Response.Status.UNAUTHORISED;
import static org.onepf.opfiab.model.event.response.Response.Status.UNKNOWN_ERROR;

public class AmazonBillingProvider extends BaseBillingProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(AmazonBillingProvider.class);

    private static final String NAME = "Amazon";
    private static final String PACKAGE_NAME = "com.amazon.venezia";


    private final AmazonBillingController controller = new AmazonBillingController();
    private final BillingProviderInfo info = new BillingProviderInfo(NAME, PACKAGE_NAME);

    @Nullable
    private UserData userData;

    protected AmazonBillingProvider(
            @NonNull final PurchaseVerifier purchaseVerifier,
            @NonNull final SkuResolver skuResolver) {
        super(purchaseVerifier, skuResolver);
    }

    @NonNull
    @Override
    public BillingProviderInfo getInfo() {
        return info;
    }

    @NonNull
    @Override
    public BillingController getController() {
        return controller;
    }

    private boolean checkAuthorisation() {
        userData = controller.getUserData();
        if (userData == null) {
            postResponse(UNAUTHORISED);
            return false;
        }
        return true;
    }

    @Override
    public void skuDetails(@NonNull final Collection<String> skus) {
        if (!checkAuthorisation()) {
            return;
        }
        final Set<String> resolvedSkus = OPFIabUtils.resolveSkus(skuResolver, skus);
        final ProductDataResponse productDataResponse = controller.getProductData(resolvedSkus);
        final ProductDataResponse.RequestStatus status;
        switch (status = productDataResponse.getRequestStatus()) {
            case SUCCESSFUL:
                //TODO
                final SkusDetails skusDetails = new SkusDetails(null, null);
                postResponse(SUCCESS, skusDetails);
                break;
            case FAILED:
            case NOT_SUPPORTED:
                LOGGER.error("Product data request failed.", status, productDataResponse);
                postResponse(UNKNOWN_ERROR);
                break;
        }
    }

    @Override
    public void inventory() {
        if (!checkAuthorisation()) {
            return;
        }
        final PurchaseUpdatesResponse purchaseUpdatesResponse = controller.getPurchaseUpdates();
        final PurchaseUpdatesResponse.RequestStatus status;
        switch (status = purchaseUpdatesResponse.getRequestStatus()) {
            case SUCCESSFUL:
                // Map sku
                final Inventory inventory = new Inventory(Collections.<Purchase>emptyList());
                postResponse(SUCCESS, inventory);
                break;
            case FAILED:
            case NOT_SUPPORTED:
                LOGGER.error("Purchase updates request failed.", status, purchaseUpdatesResponse);
                postResponse(UNKNOWN_ERROR);
                break;
        }

    }

    @Override
    public void purchase(@NonNull final Activity activity, @NonNull final SkuDetails skuDetails) {
        if (!checkAuthorisation()) {
            return;
        }
        final String resolvedSku = skuResolver.resolve(skuDetails.getSku());
        final PurchaseResponse purchaseResponse = controller.getPurchase(resolvedSku);
        final PurchaseResponse.RequestStatus status;
        switch (status = purchaseResponse.getRequestStatus()) {
            case SUCCESSFUL:
                final Purchase purchase = getPurchase(purchaseResponse.getReceipt());
                postResponse(SUCCESS, purchase);
                break;
            case INVALID_SKU:
                postResponse(ITEM_UNAVAILABLE);
                break;
            case ALREADY_PURCHASED:
                postResponse(ITEM_ALREADY_OWNED);
                break;
            case FAILED:
            case NOT_SUPPORTED:
                LOGGER.error("Purchase request failed.", status, purchaseResponse);
                postResponse(UNKNOWN_ERROR);
                break;
        }
    }

    @Override
    public void consume(@NonNull final ConsumableDetails consumableDetails) {
        if (!checkAuthorisation()) {
            return;
        }
        final String resolvedSku = consumableDetails.getSku();
        controller.consume(resolvedSku);
        postResponse(SUCCESS, consumableDetails);
    }


    private Purchase getPurchase(@NonNull final Receipt receipt) {
        //TODO
        return new ConsumablePurchase(new ConsumableDetails(receipt.getSku()));
    }

    public static class Builder extends BaseBillingProvider.Builder {

        @Override
        public BaseBillingProvider build() {
            return new AmazonBillingProvider(purchaseVerifier, skuResolver);
        }
    }
}
