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

package org.onepf.opfiab.model.billing;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;
import org.onepf.opfiab.model.BillingProviderInfo;
import org.onepf.opfutils.OPFLog;

import static org.json.JSONObject.NULL;

public class Purchase extends BillingModel {

    private static final String NAME_TOKEN = "token";
    private static final String NAME_PURCHASE_TIME = "purchaseTime";
    private static final String NAME_CANCELED = "canceled";


    @Nullable
    private final String token;
    private final long purchaseTime;
    private final boolean canceled;

    protected Purchase(@NonNull final String sku,
                       @Nullable final SkuType type,
                       @Nullable final BillingProviderInfo info,
                       @Nullable final String originalJson,
                       @Nullable final String token,
                       final long purchaseTime,
                       final boolean canceled) {
        super(sku, type, info, originalJson);
        this.token = token;
        this.purchaseTime = purchaseTime;
        this.canceled = canceled;
    }

    public Purchase(@NonNull final String sku) {
        this(sku, null, null, null, null, -1L, false);
    }

    @Nullable
    public String getToken() {
        return token;
    }

    public long getPurchaseTime() {
        return purchaseTime;
    }

    public boolean isCanceled() {
        return canceled;
    }

    @NonNull
    @Override
    public JSONObject toJson() {
        final JSONObject jsonObject = super.toJson();
        try {
            jsonObject.put(NAME_TOKEN, token == null ? NULL : token);
            jsonObject.put(NAME_PURCHASE_TIME, purchaseTime);
            jsonObject.put(NAME_CANCELED, canceled);
        } catch (JSONException exception) {
            OPFLog.e("", exception);
        }
        return jsonObject;
    }

    public static class Builder extends BillingModel.Builder {

        @Nullable
        private String token;
        private boolean canceled;
        private long purchaseTime = -1L;

        public Builder(@NonNull final String sku) {
            super(sku);
        }

        @Override
        public Builder setType(@Nullable final SkuType type) {
            return (Builder) super.setType(type);
        }

        @Override
        public Builder setProviderInfo(
                @Nullable final BillingProviderInfo providerInfo) {
            return (Builder) super.setProviderInfo(providerInfo);
        }

        @Override
        public Builder setOriginalJson(@Nullable final String originalJson) {
            return (Builder) super.setOriginalJson(originalJson);
        }

        @Override
        public Builder setBillingModel(@NonNull final BillingModel billingModel) {
            return (Builder) super.setBillingModel(billingModel);
        }

        public Builder setToken(@Nullable final String token) {
            this.token = token;
            return this;
        }

        public Builder setCanceled(final boolean canceled) {
            this.canceled = canceled;
            return this;
        }

        public Builder setPurchaseTime(final long purchaseTime) {
            this.purchaseTime = purchaseTime;
            return this;
        }

        public Builder setPurchase(@NonNull final Purchase purchase) {
            setBillingModel(purchase);
            setToken(purchase.getToken());
            setPurchaseTime(purchase.getPurchaseTime());
            setCanceled(purchase.isCanceled());
            return this;
        }

        @Override
        public Purchase build() {
            return new Purchase(sku, type, providerInfo, originalJson, token, purchaseTime,
                                canceled);
        }
    }
}
