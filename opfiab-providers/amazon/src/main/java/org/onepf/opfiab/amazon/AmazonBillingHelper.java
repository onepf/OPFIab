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

import com.amazon.device.iap.PurchasingListener;
import com.amazon.device.iap.PurchasingService;
import com.amazon.device.iap.model.ProductDataResponse;
import com.amazon.device.iap.model.PurchaseResponse;
import com.amazon.device.iap.model.PurchaseUpdatesResponse;
import com.amazon.device.iap.model.UserData;
import com.amazon.device.iap.model.UserDataResponse;

import org.onepf.opfiab.OPFIab;
import org.onepf.opfiab.util.SyncedReference;
import org.onepf.opfutils.OPFLog;

/**
 * This class handles all communications between library and Amazon SDK.
 * <p/>
 * Intended to exist as singleton, which is registered for Amazon callbacks as soon as it's
 * created.
 */
final class AmazonBillingHelper implements PurchasingListener {

    /**
     * Timeout to give up on waiting for user data.
     */
    private static final int USER_DATA_TIMEOUT = 1000;

    private static AmazonBillingHelper instance;

    @SuppressWarnings("PMD.NonThreadSafeSingleton")
    public static AmazonBillingHelper getInstance(@NonNull final Context context) {
        if (instance == null) {
            instance = new AmazonBillingHelper();
            PurchasingService.registerListener(context, instance);
        }
        return instance;
    }

    // User data is requested from library thread, but delivered on main.
    @Nullable
    private volatile SyncedReference<UserData> syncUserData;

    private AmazonBillingHelper() {
        super();
    }

    /**
     * Requests user data form Amazon SDK.
     *
     * @return User data if received withing {@link #USER_DATA_TIMEOUT}, null otherwise.
     */
    @Nullable
    UserData getUserData() {
        final SyncedReference<UserData> syncUserData = new SyncedReference<>();
        try {
            this.syncUserData = syncUserData;
            PurchasingService.getUserData();
            return syncUserData.get(USER_DATA_TIMEOUT);
        } finally {
            this.syncUserData = null;
        }
    }

    @Override
    public void onUserDataResponse(@NonNull final UserDataResponse userDataResponse) {
        OPFLog.logMethod(userDataResponse);
        final SyncedReference<UserData> syncUserData = this.syncUserData;
        if (syncUserData == null) {
            return;
        }
        switch (userDataResponse.getRequestStatus()) {
            case SUCCESSFUL:
                syncUserData.set(userDataResponse.getUserData());
                break;
            case FAILED:
            case NOT_SUPPORTED:
                OPFLog.e("UserData request failed: %s", userDataResponse);
                break;
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
