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

/**
 * This class handles all communications between library and Amazon SDK.
 * <p>
 * Intended to exist as singleton, which is registered for Amazon callbacks as soon as it's
 * created.
 */
final class AmazonBillingHelper implements PurchasingListener {

    /**
     * Timeout to give up on waiting for user data.
     */
    private static final int USER_DATA_TIMEOUT = 1000;

    // User data is requested from library thread, but delivered on main.
    @Nullable
    private volatile CountDownLatch userDataLatch;
    @Nullable
    private volatile UserData userData;

    AmazonBillingHelper() {
        super();
    }

    /**
     * Request user data form Amazon SDK.
     *
     * @return User data if received withing {@link #USER_DATA_TIMEOUT}, null otherwise.
     */
    @Nullable
    UserData getUserData() {
        // TODO check re-login
        OPFChecks.checkThread(false);
        final UserData localUserData = userData;
        if (localUserData != null) {
            return localUserData;
        }

        if (userDataLatch != null) {
            // Might happen if library handles request in multithreaded pool
            throw new IllegalStateException("There must be no concurrent requests.");
        }

        try {
            userDataLatch = new CountDownLatch(1);
            PurchasingService.getUserData();
            //noinspection ConstantConditions
            if (!userDataLatch.await(USER_DATA_TIMEOUT, TimeUnit.MILLISECONDS)) {
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
        OPFLog.logMethod(userDataResponse);
        switch (userDataResponse.getRequestStatus()) {
            case SUCCESSFUL:
                userData = userDataResponse.getUserData();
                break;
            case FAILED:
            case NOT_SUPPORTED:
                userData = null;
                OPFLog.e("UserData request failed: %s", userDataResponse);
                break;
            default:
                throw new IllegalStateException();
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
