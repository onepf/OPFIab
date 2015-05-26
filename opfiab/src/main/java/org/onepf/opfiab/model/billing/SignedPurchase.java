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

/**
 * Purchase that additionally contains cryptographic signature which indicates that this purchase is
 * legit.
 */
public class SignedPurchase extends Purchase {

    private static final String NAME_SIGNATURE = "signature";


    @Nullable
    private final String signature;

    public SignedPurchase(@NonNull final String sku,
                          @Nullable final SkuType type,
                          @Nullable final BillingProviderInfo info,
                          @Nullable final String originalJson,
                          @Nullable final String token,
                          final long purchaseTime, final boolean canceled,
                          @Nullable final String signature) {
        super(sku, type, info, originalJson, token, purchaseTime, canceled);
        this.signature = signature;
    }

    public SignedPurchase(@NonNull final String sku) {
        super(sku);
        this.signature = null;
    }

    /**
     * Gets this purchase signature. Used for purchase verification.
     *
     * @return Purchase signature, can be null.
     */
    @Nullable
    public String getSignature() {
        return signature;
    }

    @NonNull
    @Override
    public Purchase substituteSku(@NonNull final String sku) {
        return new Builder(sku).setSignedPurchase(this).build();
    }

    @NonNull
    @Override
    public JSONObject toJson() {
        final JSONObject jsonObject = super.toJson();
        try {
            jsonObject.put(NAME_SIGNATURE, signature == null ? NULL : signature);
        } catch (JSONException exception) {
            OPFLog.e("", exception);
        }
        return jsonObject;
    }

    public static class Builder extends Purchase.Builder {

        private String signature;

        public Builder(@NonNull final String sku) {
            super(sku);
        }

        public Builder setSignedPurchase(@NonNull final SignedPurchase signedPurchase) {
            super.setPurchase(signedPurchase);
            setSignature(signedPurchase.getSignature());
            return this;
        }

        public Builder setSignature(@Nullable final String signature) {
            this.signature = signature;
            return this;
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

        @Override
        public Builder setToken(@Nullable final String token) {
            return (Builder) super.setToken(token);
        }

        @Override
        public Builder setCanceled(final boolean canceled) {
            return (Builder) super.setCanceled(canceled);
        }

        @Override
        public Builder setPurchaseTime(final long purchaseTime) {
            return (Builder) super.setPurchaseTime(purchaseTime);
        }

        @Override
        public Builder setPurchase(@NonNull final Purchase purchase) {
            return (Builder) super.setPurchase(purchase);
        }

        @Override
        public SignedPurchase build() {
            return new SignedPurchase(sku, type, providerInfo, originalJson, token, purchaseTime,
                                      canceled, signature);
        }
    }
}
