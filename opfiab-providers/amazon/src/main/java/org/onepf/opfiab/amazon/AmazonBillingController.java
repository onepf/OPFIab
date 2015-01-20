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
import com.amazon.device.iap.internal.model.PurchaseUpdatesResponseBuilder;
import com.amazon.device.iap.model.FulfillmentResult;
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
import java.util.List;
import java.util.Set;
import java.util.concurrent.Semaphore;

final class AmazonBillingController implements BillingController {

    private static final Logger LOGGER = LoggerFactory.getLogger(AmazonBillingController.class);


    @NonNull
    private final Semaphore semaphore = new Semaphore(0);

    @Nullable
    private volatile UserData userData;
    @NonNull
    private volatile ProductDataResponse productData;
    @NonNull
    private volatile PurchaseResponse purchase;
    @NonNull
    private volatile PurchaseUpdatesResponse purchaseUpdates;
    @NonNull
    private volatile List<Receipt> receipts;

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

    @NonNull
    ProductDataResponse getProductData(@NonNull final Set<String> skus) {
        checkState();
        PurchasingService.getProductData(skus);
        semaphore.acquireUninterruptibly();
        return productData;
    }

    @NonNull
    PurchaseUpdatesResponse getPurchaseUpdates() {
        checkState();
        receipts = new ArrayList<>();
        PurchasingService.getPurchaseUpdates(true);
        semaphore.acquireUninterruptibly();
        return purchaseUpdates;
    }

    @NonNull
    PurchaseResponse getPurchase(@NonNull final String sku) {
        checkState();
        PurchasingService.purchase(sku);
        semaphore.acquireUninterruptibly();
        return purchase;
    }

    void consume(@NonNull final String sku) {
        PurchasingService.notifyFulfillment(sku, FulfillmentResult.FULFILLED);
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
            productData = productDataResponse;
            semaphore.release();
        }

        @Override
        public void onPurchaseResponse(@NonNull final PurchaseResponse purchaseResponse) {
            purchase = purchaseResponse;
            semaphore.release();
        }

        @Override
        public void onPurchaseUpdatesResponse(
                @NonNull final PurchaseUpdatesResponse purchaseUpdatesResponse) {
            if (purchaseUpdatesResponse.getRequestStatus() == PurchaseUpdatesResponse.RequestStatus.SUCCESSFUL) {
                receipts.addAll(purchaseUpdatesResponse.getReceipts());
                if (purchaseUpdatesResponse.hasMore()) {
                    PurchasingService.getPurchaseUpdates(true);
                    return;
                }
                purchaseUpdates = new PurchaseUpdatesResponseBuilder()
                        .setReceipts(receipts)
                        .setHasMore(purchaseUpdatesResponse.hasMore())
                        .setRequestId(purchaseUpdatesResponse.getRequestId())
                        .setRequestStatus(purchaseUpdatesResponse.getRequestStatus())
                        .setUserData(purchaseUpdatesResponse.getUserData())
                        .build();
            } else {
                purchaseUpdates = purchaseUpdatesResponse;
            }
            semaphore.release();
        }
    }
}
