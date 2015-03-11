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

import org.json.JSONException;
import org.json.JSONObject;

public class GooglePurchase extends GoogleModel {

    private static final String NAME_ORDER_ID = "";
    private static final String NAME_PACKAGE_NAME = "";
    private static final String NAME_PURCHASE_STATE = "";
    private static final String NAME_DEVELOPER_PAYLOAD = "";
    private static final String NAME_PURCHASE_TOKEN = "";
    private static final String NAME_PURCHASE_TIME = "";
    private static final String NAME_AUTO_RENEWING = "";


    @NonNull
    private final String orderId;
    @NonNull
    private final String packageName;
    @NonNull
    private final String developerPayload;
    @NonNull
    private final String purchaseToken;
    private final long purchaseTime;
    private final boolean autoRenewing;
    @NonNull
    private final PurchaseState purchaseState;


    public GooglePurchase(@NonNull final JSONObject json) throws JSONException {
        super(json);
        this.orderId = json.getString(NAME_ORDER_ID);
        this.packageName = json.getString(NAME_PACKAGE_NAME);
        this.developerPayload = json.getString(NAME_DEVELOPER_PAYLOAD);
        this.purchaseToken = json.getString(NAME_PURCHASE_TOKEN);
        this.purchaseTime = json.getLong(NAME_PURCHASE_TIME);
        this.autoRenewing = json.getBoolean(NAME_AUTO_RENEWING);

        final int purchaseStateCode = json.getInt(NAME_PURCHASE_STATE);
        final PurchaseState purchaseState = PurchaseState.fromCode(purchaseStateCode);
        if (purchaseState == null) {
            throw new JSONException("Unrecognized purchase state: " + purchaseStateCode);
        }
        this.purchaseState = purchaseState;
    }

    public GooglePurchase(@NonNull final String json) throws JSONException {
        this(new JSONObject(json));
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
    public String getDeveloperPayload() {
        return developerPayload;
    }

    @NonNull
    public String getPurchaseToken() {
        return purchaseToken;
    }

    public long getPurchaseTime() {
        return purchaseTime;
    }

    public boolean isAutoRenewing() {
        return autoRenewing;
    }

    @NonNull
    public PurchaseState getPurchaseState() {
        return purchaseState;
    }
}
