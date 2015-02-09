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

import android.content.Context;
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

import org.onepf.opfiab.billing.BillingController;
import org.onepf.opfutils.OPFLog;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Semaphore;

final class AmazonBillingController implements BillingController, PurchasingListener {

    @NonNull
    private final Semaphore semaphore = new Semaphore(0);

    @Nullable
    private volatile UserData userData;
    @Nullable
    private volatile PurchaseUpdatesResponse purchaseUpdates;
    @NonNull
    private volatile ProductDataResponse productData;
    @NonNull
    private volatile PurchaseResponse purchase;

    public AmazonBillingController(@NonNull final Context context) {
        PurchasingService.registerListener(context, this);
    }

    @Override
    public boolean isBillingSupported() {
        return true;
    }

    @Override
    public boolean isAuthorised() {
        final UserData userData;
        userData = getUserData();
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
        purchaseUpdates = null;
        PurchasingService.getPurchaseUpdates(true);
        semaphore.acquireUninterruptibly();
        if (purchaseUpdates == null) {
            throw new IllegalStateException();
        }
        //noinspection ConstantConditions
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

    @Override
    public void onUserDataResponse(@NonNull final UserDataResponse userDataResponse) {
        switch (userDataResponse.getRequestStatus()) {
            case SUCCESSFUL:
                userData = userDataResponse.getUserData();
                break;
            case FAILED:
            case NOT_SUPPORTED:
                userData = null;
                OPFLog.d("UserData request failed: %s", userDataResponse);
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
        final PurchaseUpdatesResponse.RequestStatus requestStatus = purchaseUpdatesResponse.getRequestStatus();
        final List<Receipt> receipts = new ArrayList<>(purchaseUpdatesResponse.getReceipts());
        if (purchaseUpdates != null) {
            receipts.addAll(purchaseUpdates.getReceipts());
        }
        purchaseUpdates = new PurchaseUpdatesResponseBuilder()
                .setReceipts(receipts)
                .setHasMore(purchaseUpdatesResponse.hasMore())
                .setRequestId(purchaseUpdatesResponse.getRequestId())
                .setRequestStatus(requestStatus)
                .setUserData(purchaseUpdatesResponse.getUserData())
                .build();
        if (requestStatus == PurchaseUpdatesResponse.RequestStatus.SUCCESSFUL
                && purchaseUpdatesResponse.hasMore()) {
            PurchasingService.getPurchaseUpdates(true);
        } else {
            semaphore.release();
        }
    }
}
