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
import org.onepf.opfutils.OPFLog;

import static org.json.JSONObject.NULL;

/**
 * Model class representing purchase made by user.
 */
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
                       @Nullable final String providerName,
                       @Nullable final String originalJson,
                       @Nullable final String token,
                       final long purchaseTime,
                       final boolean canceled) {
        super(sku, type, providerName, originalJson);
        this.token = token;
        this.purchaseTime = purchaseTime;
        this.canceled = canceled;
    }

    public Purchase(@NonNull final String sku) {
        this(sku, null, null, null, null, -1L, false);
    }

    /**
     * Gets unique token identifying the purchase.
     *
     * @return This purchase token.
     */
    @Nullable
    public String getToken() {
        return token;
    }

    /**
     * Gets date of the purchase in milliseconds.
     *
     * @return Date when this purchase was made.
     */
    public long getPurchaseTime() {
        return purchaseTime;
    }

    /**
     * Indicates whether this purchase is still valid or not.
     * <p>
     * This will return <b>true</b> if this purchase represents a subscription and it's expired.
     *
     * @return True if this purchase is no longer valid, false otherwise.
     */
    public boolean isCanceled() {
        return canceled;
    }

    @NonNull
    @Override
    public Purchase copyWithSku(@NonNull final String sku) {
        return new Builder(sku).setBase(this).build();
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

    /**
     * Builder class for {@link Purchase} object.
     */
    @SuppressWarnings("unchecked")
    protected abstract static class PurchaseBuilder<B extends PurchaseBuilder, M extends Purchase>
            extends BillingModel.Builder<B, M> {

        @Nullable
        protected String token;
        protected boolean canceled;
        protected long purchaseTime = -1L;

        public PurchaseBuilder(@NonNull final String sku) {
            super(sku);
        }

        /**
         * Sets unique token to use in a new Purchase object.
         *
         * @param token Token to use.
         * @return this object.
         */
        public B setToken(@Nullable final String token) {
            this.token = token;
            return (B) this;
        }

        /**
         * Sets canceled status of a new Purchase object.
         *
         * @param canceled Canceled status to set.
         * @return this object.
         */
        public B setCanceled(final boolean canceled) {
            this.canceled = canceled;
            return (B) this;
        }

        /**
         * Sets the time for a new Purchase object.
         *
         * @param purchaseTime Time in millisecond when purchase was made.
         * @return this object.
         */
        public B setPurchaseTime(final long purchaseTime) {
            this.purchaseTime = purchaseTime;
            return (B) this;
        }

        @Override
        public B setBase(@NonNull final M billingModel) {
            setToken(billingModel.getToken());
            setPurchaseTime(billingModel.getPurchaseTime());
            setCanceled(billingModel.isCanceled());
            return super.setBase(billingModel);
        }
    }

    public static class Builder extends PurchaseBuilder<Builder, Purchase> {

        public Builder(@NonNull final String sku) {
            super(sku);
        }

        @Override
        public Purchase build() {
            return new Purchase(sku, type, providerName, originalJson, token, purchaseTime,
                                canceled);
        }
    }
}
