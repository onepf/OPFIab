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
import android.support.annotation.Nullable;

import com.amazon.device.iap.model.ProductDataResponse;
import com.amazon.device.iap.model.PurchaseResponse;
import com.amazon.device.iap.model.PurchaseUpdatesResponse;
import com.amazon.device.iap.model.Receipt;
import com.amazon.device.iap.model.UserData;

import org.onepf.opfiab.BaseBillingProvider;
import org.onepf.opfiab.OPFIabUtils;
import org.onepf.opfiab.model.BillingProviderInfo;
import org.onepf.opfiab.model.billing.Purchase;
import org.onepf.opfiab.sku.SkuResolver;
import org.onepf.opfiab.verification.PurchaseVerifier;

import java.util.Set;

import static org.onepf.opfiab.model.event.billing.Response.Status.ITEM_ALREADY_OWNED;
import static org.onepf.opfiab.model.event.billing.Response.Status.ITEM_UNAVAILABLE;
import static org.onepf.opfiab.model.event.billing.Response.Status.SUCCESS;
import static org.onepf.opfiab.model.event.billing.Response.Status.UNAUTHORISED;
import static org.onepf.opfiab.model.event.billing.Response.Status.UNKNOWN_ERROR;

public class AmazonBillingProvider extends BaseBillingProvider {

    private static final String NAME = "Amazon";
    private static final String PACKAGE_NAME = "com.amazon.venezia";


    private final BillingProviderInfo info = new BillingProviderInfo(NAME, PACKAGE_NAME);
    @NonNull
    private final AmazonBillingController controller = new AmazonBillingController(context);

    @Nullable
    private UserData userData;

    protected AmazonBillingProvider(
            @NonNull final Context context,
            @NonNull final PurchaseVerifier purchaseVerifier,
            @NonNull final SkuResolver skuResolver) {
        super(context, purchaseVerifier, skuResolver);
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

    private boolean checkAuthorisation() {
        userData = controller.getUserData();
        if (userData == null) {
            postResponse(UNAUTHORISED);
            return false;
        }
        return true;
    }

    @Override
    public void skuDetails(@NonNull final Set<String> skus) {
        if (!checkAuthorisation()) {
            return;
        }
        final Set<String> resolvedSkus = OPFIabUtils.resolveSkus(skuResolver, skus);
        final ProductDataResponse productDataResponse = controller.getProductData(resolvedSkus);
        final ProductDataResponse.RequestStatus status;
        switch (status = productDataResponse.getRequestStatus()) {
            case SUCCESSFUL:
                //TODO
                postResponse(SUCCESS);
                break;
            case FAILED:
            case NOT_SUPPORTED:
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
                //TODO Map sku
                postResponse(SUCCESS);
                break;
            case FAILED:
            case NOT_SUPPORTED:
                postResponse(UNKNOWN_ERROR);
                break;
        }

    }

    @Override
    public void purchase(@NonNull final Activity activity, @NonNull final String sku) {
        if (!checkAuthorisation()) {
            return;
        }
        final PurchaseResponse purchaseResponse = controller.getPurchase(sku);
        final PurchaseResponse.RequestStatus status;
        switch (status = purchaseResponse.getRequestStatus()) {
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
            case NOT_SUPPORTED:
                postResponse(UNKNOWN_ERROR);
                break;
        }
    }

    @Override
    public void consume(@NonNull final Purchase purchase) {
        if (!checkAuthorisation()) {
            return;
        }
        controller.consume(purchase.getSku());
        postResponse(SUCCESS);
    }


    private Purchase newPurchase(@NonNull final Receipt receipt) {
        //        final Purchase purchase;
        //        switch (receipt.getProductType()) {
        //            case CONSUMABLE:
        //                purchase = new ConsumablePurchase();
        //                break;
        //            case ENTITLED:
        //                break;
        //            case SUBSCRIPTION:
        //                break;
        //        }

        return null;
    }



    public static class Builder extends BaseBillingProvider.Builder {

        public Builder(@NonNull final Context context) {
            super(context);
        }

        @Override
        public BaseBillingProvider build() {
            return new AmazonBillingProvider(context, purchaseVerifier, skuResolver);
        }
    }
}
