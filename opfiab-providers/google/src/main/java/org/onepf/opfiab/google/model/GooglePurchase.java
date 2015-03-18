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
import org.json.JSONObject;

public class GooglePurchase extends GoogleModel {

    private static final String NAME_ORDER_ID = "orderId";
    private static final String NAME_PACKAGE_NAME = "packageName";
    private static final String NAME_PURCHASE_TOKEN = "purchaseState";
    private static final String NAME_PURCHASE_STATE = "productId";
    private static final String NAME_PURCHASE_TIME = "purchaseTime";
    private static final String NAME_AUTO_RENEWING = "autoRenewing";
    private static final String NAME_DEVELOPER_PAYLOAD = "developerPayload";


    @NonNull
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


    public GooglePurchase(@NonNull final String originalJson, @NonNull final JSONObject jsonObject)
            throws JSONException {
        super(originalJson, jsonObject);
        this.orderId = jsonObject.getString(NAME_ORDER_ID);
        this.packageName = jsonObject.getString(NAME_PACKAGE_NAME);
        this.purchaseToken = jsonObject.getString(NAME_PURCHASE_TOKEN);
        this.purchaseTime = jsonObject.getLong(NAME_PURCHASE_TIME);
        this.autoRenewing = jsonObject.getBoolean(NAME_AUTO_RENEWING);
        this.developerPayload = jsonObject.has(NAME_DEVELOPER_PAYLOAD)
                ? jsonObject.optString(NAME_DEVELOPER_PAYLOAD)
                : null;

        final int purchaseStateCode = jsonObject.getInt(NAME_PURCHASE_STATE);
        final PurchaseState purchaseState = PurchaseState.fromCode(purchaseStateCode);
        if (purchaseState == null) {
            throw new JSONException("Unrecognized purchase state: " + purchaseStateCode);
        }
        this.purchaseState = purchaseState;
    }

    public GooglePurchase(@NonNull final String originalJson)
            throws JSONException {
        this(originalJson, new JSONObject(originalJson));
    }


    @NonNull
    public String getOrderId() {
        return orderId;
    }

    @NonNull
    public String getPackageName() {
        return packageName;
    }

    @NonNull
    public String getPurchaseToken() {
        return purchaseToken;
    }

    @NonNull
    public PurchaseState getPurchaseState() {
        return purchaseState;
    }

    @Nullable
    public String getDeveloperPayload() {
        return developerPayload;
    }

    public long getPurchaseTime() {
        return purchaseTime;
    }

    public boolean isAutoRenewing() {
        return autoRenewing;
    }
}
