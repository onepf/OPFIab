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

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.amazon.device.iap.PurchasingListener;
import com.amazon.device.iap.PurchasingService;
import com.amazon.device.iap.model.Product;
import com.amazon.device.iap.model.ProductDataResponse;
import com.amazon.device.iap.model.PurchaseResponse;
import com.amazon.device.iap.model.PurchaseUpdatesResponse;
import com.amazon.device.iap.model.Receipt;
import com.amazon.device.iap.model.UserData;
import com.amazon.device.iap.model.UserDataResponse;

import org.onepf.opfiab.OPFIab;
import org.onepf.opfiab.billing.BillingController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Semaphore;

final class AmazonBillingController implements BillingController {

    private static final Logger LOGGER = LoggerFactory.getLogger(AmazonBillingController.class);


    @NonNull
    private final Semaphore semaphore = new Semaphore(0);

    @Nullable
    private volatile UserData userData;

    @Nullable
    private volatile Map<String, Product> productData;

    @Nullable
    private volatile List<Receipt> purchaseUpdates;

    @Nullable
    private volatile Receipt purchase;

    public AmazonBillingController() {
        PurchasingService.registerListener(OPFIab.getContext(), new Listener());
    }

    @Override
    public boolean isBillingSupported() {
        return true;
    }

    @Override
    public boolean isAuthorised() {
        final UserData userData = getUserData();
        return userData != null && !TextUtils.isEmpty(userData.getUserId());
    }

    private void checkState() {
        if (semaphore.availablePermits() != 0) {
            throw new IllegalStateException("There must not be any concurrent requests.");
        }
    }

    @Nullable
    UserData getUserData() {
        checkState();
        userData = null;
        PurchasingService.getUserData();
        semaphore.acquireUninterruptibly();
        return userData;
    }

    @Nullable
    Map<String, Product> getProductData(@NonNull final Set<String> skus) {
        checkState();
        productData = new HashMap<>();
        PurchasingService.getProductData(skus);
        semaphore.acquireUninterruptibly();
        return productData;
    }

    @Nullable
    List<Receipt> getPurchaseUpdates() {
        checkState();
        purchaseUpdates = new ArrayList<>();
        PurchasingService.getPurchaseUpdates(true);
        semaphore.acquireUninterruptibly();
        return purchaseUpdates;
    }

    @Nullable
    Receipt getPurchase(@NonNull final String sku) {
        checkState();
        purchase = null;
        PurchasingService.purchase(sku);
        semaphore.acquireUninterruptibly();
        return purchase;
    }

    private final class Listener implements PurchasingListener {

        @Override
        public void onUserDataResponse(@NonNull final UserDataResponse userDataResponse) {
            final UserDataResponse.RequestStatus requestStatus;
            switch (requestStatus = userDataResponse.getRequestStatus()) {
                case SUCCESSFUL:
                    userData = userDataResponse.getUserData();
                    break;
                case FAILED:
                case NOT_SUPPORTED:
                    userData = null;
                    LOGGER.error("User data request failed.", requestStatus, userDataResponse);
                    break;
            }
            semaphore.release();
        }

        @Override
        public void onProductDataResponse(@NonNull final ProductDataResponse productDataResponse) {
            final ProductDataResponse.RequestStatus requestStatus;
            switch (requestStatus = productDataResponse.getRequestStatus()) {
                case SUCCESSFUL:
                    //noinspection ConstantConditions
                    productData.putAll(productDataResponse.getProductData());
                    final Collection<String> unavailableSkus = productDataResponse.getUnavailableSkus();
                    for (final String sku : unavailableSkus) {
                        //noinspection ConstantConditions
                        productData.put(sku, null);
                    }
                    break;
                case FAILED:
                case NOT_SUPPORTED:
                    productData = null;
                    LOGGER.error("Product data request failed.", requestStatus,
                                 productDataResponse);
                    break;
            }
            semaphore.release();
        }

        @Override
        public void onPurchaseResponse(@NonNull final PurchaseResponse purchaseResponse) {
            final PurchaseResponse.RequestStatus requestStatus;
            //TODO handle all response codes. Manually construct receipt.
            switch (requestStatus = purchaseResponse.getRequestStatus()) {
                case SUCCESSFUL:
                    purchase = purchaseResponse.getReceipt();
                    break;
                case INVALID_SKU:
                    break;
                case ALREADY_PURCHASED:
                    break;
                case FAILED:
                case NOT_SUPPORTED:
                    purchase = null;
                    LOGGER.error("Purchase request failed.", requestStatus, purchaseResponse);
                    break;
            }
            semaphore.release();
        }

        @Override
        public void onPurchaseUpdatesResponse(
                @NonNull final PurchaseUpdatesResponse purchaseUpdatesResponse) {
            final PurchaseUpdatesResponse.RequestStatus requestStatus;
            switch (requestStatus = purchaseUpdatesResponse.getRequestStatus()) {
                case SUCCESSFUL:
                    //noinspection ConstantConditions
                    purchaseUpdates.addAll(purchaseUpdatesResponse.getReceipts());
                    if (purchaseUpdatesResponse.hasMore()) {
                        PurchasingService.getPurchaseUpdates(true);
                        return;
                    }
                    break;
                case FAILED:
                case NOT_SUPPORTED:
                    purchaseUpdates = null;
                    LOGGER.error("Purchase updates request failed.", requestStatus,
                                 purchaseUpdatesResponse);
                    break;
            }
            semaphore.release();
        }
    }
}
