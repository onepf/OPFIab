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

import com.amazon.device.iap.PurchasingListener;
import com.amazon.device.iap.PurchasingService;
import com.amazon.device.iap.model.ProductDataResponse;
import com.amazon.device.iap.model.PurchaseResponse;
import com.amazon.device.iap.model.PurchaseUpdatesResponse;
import com.amazon.device.iap.model.UserData;
import com.amazon.device.iap.model.UserDataResponse;

import org.onepf.opfiab.OPFIab;
import org.onepf.opfutils.OPFChecks;
import org.onepf.opfutils.OPFLog;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

final class AmazonBillingHelper implements PurchasingListener {

    @SuppressWarnings({"checkstyle:magicnumber"})
    private static final int TIMEOUT = PurchasingService.IS_SANDBOX_MODE ? 60000 : 5000;


    @Nullable
    private volatile CountDownLatch userDataLatch;
    @Nullable
    private volatile UserData userData;

    AmazonBillingHelper() {
        super();
    }

    @Nullable
    UserData getUserData() {
        OPFChecks.checkThread(false);
        final UserData localUserData = userData;
        if (localUserData != null) {
            return localUserData;
        }

        if (userDataLatch != null) {
            throw new IllegalStateException("There must be no concurrent requests.");
        }

        try {
            userDataLatch = new CountDownLatch(1);
            PurchasingService.getUserData();
            //noinspection ConstantConditions
            if (!userDataLatch.await(TIMEOUT, TimeUnit.MILLISECONDS)) {
                OPFLog.e("User data request timed out.");
            }
        } catch (InterruptedException exception) {
            OPFLog.e("User data request interrupted.", exception);
        } finally {
            userDataLatch = null;
        }
        return userData;
    }

    @Override
    public void onUserDataResponse(@NonNull final UserDataResponse userDataResponse) {
        OPFLog.methodD(userDataResponse);
        switch (userDataResponse.getRequestStatus()) {
            case SUCCESSFUL:
                userData = userDataResponse.getUserData();
                break;
            case FAILED:
            case NOT_SUPPORTED:
                userData = null;
                OPFLog.e("UserData request failed: %s", userDataResponse);
                break;
        }
        final CountDownLatch latch = userDataLatch;
        if (latch != null) {
            latch.countDown();
        }
    }

    @Override
    public void onProductDataResponse(@NonNull final ProductDataResponse productDataResponse) {
        OPFIab.post(productDataResponse);
    }

    @Override
    public void onPurchaseResponse(
            @NonNull final PurchaseResponse purchaseResponse) {
        OPFIab.post(purchaseResponse);
    }

    @Override
    public void onPurchaseUpdatesResponse(
            @NonNull final PurchaseUpdatesResponse purchaseUpdatesResponse) {
        OPFIab.post(purchaseUpdatesResponse);
    }
}
