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
 * Purchase that additionally contains cryptographic signature which indicates that this purchase is
 * legit.
 */
public class SignedPurchase extends Purchase {

    private static final String NAME_SIGNATURE = "signature";


    @Nullable
    private final String signature;

    @SuppressWarnings({"checkstyle:parameternumber"})
    protected SignedPurchase(@NonNull final String sku,
                          @Nullable final SkuType type,
                          @Nullable final String providerName,
                          @Nullable final String originalJson,
                          @Nullable final String token,
                          final long purchaseTime,
                          final boolean canceled,
                          @Nullable final String signature) {
        super(sku, type, providerName, originalJson, token, purchaseTime, canceled);
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
    public Purchase copyWithSku(@NonNull final String sku) {
        return new Builder(sku).setBase(this).build();
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


    @SuppressWarnings("unchecked")
    protected static abstract class SignedPurchaseBuilder<B extends SignedPurchaseBuilder,
            M extends SignedPurchase> extends Purchase.PurchaseBuilder<B, M> {

        protected String signature;

        public SignedPurchaseBuilder(@NonNull final String sku) {
            super(sku);
        }

        public B setSignature(@Nullable final String signature) {
            this.signature = signature;
            return (B) this;
        }

        @Override
        public B setBase(@NonNull final M billingModel) {
            setSignature(billingModel.getSignature());
            return super.setBase(billingModel);
        }
    }

    public static class Builder extends SignedPurchaseBuilder<Builder, SignedPurchase> {

        public Builder(@NonNull final String sku) {
            super(sku);
        }

        @Override
        public SignedPurchase build() {
            return new SignedPurchase(sku, type, providerName, originalJson, token, purchaseTime,
                    canceled, signature);
        }
    }
}
