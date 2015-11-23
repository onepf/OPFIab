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

package org.onepf.opfiab.google.model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.json.JSONException;

/**
 * This model represents purchase made in Google Play.
 */
public class GooglePurchase extends GoogleModel {

    private static final String NAME_ORDER_ID = "orderId";
    private static final String NAME_PACKAGE_NAME = "packageName";
    private static final String NAME_PURCHASE_TOKEN = "purchaseToken";
    private static final String NAME_PURCHASE_STATE = "purchaseState";
    private static final String NAME_PURCHASE_TIME = "purchaseTime";
    private static final String NAME_DEVELOPER_PAYLOAD = "developerPayload";
    private static final String NAME_AUTO_RENEWING = "autoRenewing";


    @Nullable
    private final String orderId;
    @NonNull
    private final String packageName;
    @NonNull
    private final String purchaseToken;
    @NonNull
    private final PurchaseState purchaseState;
    @Nullable
    private final String developerPayload;
    private final long purchaseTime;
    private final boolean autoRenewing;


    public GooglePurchase(@NonNull final String originalJson) throws JSONException {
        super(originalJson);
        this.orderId = jsonObject.optString(NAME_ORDER_ID, null);
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
     * Gets a unique order identifier of the transaction. This identifier corresponds to the
     * Google Wallet Order ID.
     *
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
     * Gets token that uniquely identifies a purchase for a given item and user pair.
     *
     * @return Purchase token, can't be null.
     */
    @NonNull
    public String getPurchaseToken() {
        return purchaseToken;
    }

    /**
     * Gets purchase state of the order.
     *
     * @return Purchase state, can't be null.
     */
    @NonNull
    public PurchaseState getPurchaseState() {
        return purchaseState;
    }

    /**
     * A developer-specified string that contains supplemental information about an order.
     *
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
