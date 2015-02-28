/*
 * Copyright 2012-2014 One Platform Foundation
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
import org.onepf.opfiab.verification.VerificationResult;
import org.onepf.opfutils.OPFLog;

public class Purchase extends BillingModel {

    private static final String NAME_TOKEN = "token";
    private static final String NAME_PURCHASE_TIME = "purchaseTime";
    private static final String NAME_CANCELED = "canceled";
    private static final String NAME_VERIFICATION_RESULT = "verificationResult";


    @Nullable
    private final String token;
    private final long purchaseTime;
    private final boolean canceled;
    @Nullable
    private final VerificationResult verificationResult;

    protected Purchase(@NonNull final String sku,
                       @Nullable final SkuType type,
                       @Nullable final String originalJson,
                       @Nullable final String token,
                       final long purchaseTime,
                       final boolean canceled,
                       @Nullable final VerificationResult verificationResult) {
        super(sku, type, originalJson);
        this.token = token;
        this.purchaseTime = purchaseTime;
        this.canceled = canceled;
        this.verificationResult = verificationResult;
    }

    public Purchase(@NonNull final String sku) {
        this(sku, null, null, null, -1L, false, null);
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

    @Nullable
    public VerificationResult getVerificationResult() {
        return verificationResult;
    }

    @NonNull
    @Override
    public JSONObject toJson() {
        final JSONObject jsonObject = super.toJson();
        try {
            jsonObject.put(NAME_TOKEN, token == null ? JSONObject.NULL : token);
            jsonObject.put(NAME_PURCHASE_TIME, purchaseTime);
            jsonObject.put(NAME_CANCELED, canceled);
            jsonObject.put(NAME_VERIFICATION_RESULT, verificationResult == null
                    ? JSONObject.NULL
                    : verificationResult);
        } catch (JSONException exception) {
            OPFLog.e("", exception);
        }
        return jsonObject;
    }

    public static class Builder extends BillingModel.Builder {

        @Nullable
        private String token = null;
        private long purchaseTime = -1L;
        private boolean canceled = false;
        @Nullable
        private VerificationResult verificationResult;

        public Builder(@NonNull final String sku) {
            super(sku);
        }

        @Override
        public Builder setType(@Nullable final SkuType type) {
            return (Builder) super.setType(type);
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

        public Builder setPurchaseTime(final long purchaseTime) {
            this.purchaseTime = purchaseTime;
            return this;
        }

        public Builder setCanceled(final boolean canceled) {
            this.canceled = canceled;
            return this;
        }

        public Builder setVerificationResult(
                @Nullable final VerificationResult verificationResult) {
            this.verificationResult = verificationResult;
            return this;
        }

        public Builder setPurchase(@NonNull final Purchase purchase) {
            setBillingModel(purchase);
            setToken(purchase.getToken());
            setPurchaseTime(purchase.getPurchaseTime());
            setCanceled(purchase.isCanceled());
            setVerificationResult(purchase.getVerificationResult());
            return this;
        }

        @Override
        public Purchase build() {
            return new Purchase(sku, type, originalJson, token, purchaseTime, canceled,
                                verificationResult);
        }
    }
}
