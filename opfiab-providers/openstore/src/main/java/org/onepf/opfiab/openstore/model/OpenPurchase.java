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

package org.onepf.opfiab.openstore.model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.json.JSONException;

public class OpenPurchase extends OpenBillingModel {

    protected static final String NAME_ORDER_ID = "orderId";
    protected static final String NAME_PACKAGE_NAME = "packageName";
    protected static final String NAME_PURCHASE_TOKEN = "purchaseToken";
    protected static final String NAME_PURCHASE_STATE = "purchaseState";
    protected static final String NAME_PURCHASE_TIME = "purchaseTime";
    protected static final String NAME_DEVELOPER_PAYLOAD = "developerPayload";
    protected static final String NAME_AUTO_RENEWING = "autoRenewing";


    @NonNull
    protected final String orderId;
    @NonNull
    protected final String packageName;
    @NonNull
    protected final String purchaseToken;
    @NonNull
    protected final PurchaseState purchaseState;
    @Nullable
    protected final String developerPayload;
    protected final long purchaseTime;
    protected final boolean autoRenewing;


    public OpenPurchase(@NonNull final String originalJson) throws JSONException {
        super(originalJson);
        this.orderId = jsonObject.getString(NAME_ORDER_ID);
        this.packageName = jsonObject.getString(NAME_PACKAGE_NAME);
        this.purchaseToken = jsonObject.getString(NAME_PURCHASE_TOKEN);
        this.developerPayload = jsonObject.optString(NAME_DEVELOPER_PAYLOAD, null);
        this.purchaseTime = jsonObject.getLong(NAME_PURCHASE_TIME);
        this.autoRenewing = jsonObject.optBoolean(NAME_AUTO_RENEWING, false);

        final int purchaseStateCode = jsonObject.getInt(NAME_PURCHASE_STATE);
        final PurchaseState purchaseState = PurchaseState.fromCode(purchaseStateCode);
        if (purchaseState == null) {
            throw new JSONException("Unrecognized purchase state: " + purchaseStateCode);
        }
        this.purchaseState = purchaseState;
    }

    /**
     * @return Unique order ID, can't be null.
     */
    @NonNull
    public String getOrderId() {
        return orderId;
    }

    /**
     * Gets application package from which the purchase is originated.
     *
     * @return Package name, can't be null.
     */
    @NonNull
    public String getPackageName() {
        return packageName;
    }

    /**
     * @return Purchase token, can't be null.
     */
    @NonNull
    public String getPurchaseToken() {
        return purchaseToken;
    }

    /**
     * @return Purchase state, can't be null.
     */
    @NonNull
    public PurchaseState getPurchaseState() {
        return purchaseState;
    }

    /**
     * @return Developer payload, can be null.
     */
    @Nullable
    public String getDeveloperPayload() {
        return developerPayload;
    }

    /**
     * Gets the time the product was purchased, in milliseconds since the epoch (Jan 1, 1970).
     *
     * @return Time of purchase.
     */
    public long getPurchaseTime() {
        return purchaseTime;
    }

    /**
     * Indicates whether the subscription renews automatically.
     *
     * @return True if subscription is active, and will automatically renew on the next billing
     * date. False if the user has canceled the subscription.
     */
    public boolean isAutoRenewing() {
        return autoRenewing;
    }
}
