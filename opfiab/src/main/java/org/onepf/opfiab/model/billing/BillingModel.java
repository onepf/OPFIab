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
import org.onepf.opfiab.JsonCompatible;
import org.onepf.opfiab.OPFIabUtils;
import org.onepf.opfiab.model.BillingProviderInfo;
import org.onepf.opfutils.OPFLog;

import java.io.Serializable;

import static org.json.JSONObject.NULL;

public abstract class BillingModel implements JsonCompatible, Serializable {

    private static final String NAME_SKU = "sku";
    private static final String NAME_TYPE = "type";
    private static final String NAME_PROVIDER_INFO = "provider_info";
    private static final String NAME_ORIGINAL_JSON = "original_json";


    @NonNull
    private final String sku;
    @NonNull
    private final SkuType type;
    @Nullable
    private final BillingProviderInfo providerInfo;
    @Nullable
    private final String originalJson;

    protected BillingModel(@NonNull final String sku,
                           @Nullable final SkuType type,
                           @Nullable final BillingProviderInfo providerInfo,
                           @Nullable final String originalJson) {
        this.sku = sku;
        this.type = type == null ? SkuType.UNKNOWN : type;
        this.providerInfo = providerInfo;
        this.originalJson = originalJson;
    }

    @NonNull
    public String getSku() {
        return sku;
    }

    @NonNull
    public SkuType getType() {
        return type;
    }

    @Nullable
    public BillingProviderInfo getProviderInfo() {
        return providerInfo;
    }

    @Nullable
    public String getOriginalJson() {
        return originalJson;
    }

    @NonNull
    @Override
    public JSONObject toJson() {
        final JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(NAME_SKU, sku);
            jsonObject.put(NAME_TYPE, type);
            jsonObject.put(NAME_PROVIDER_INFO, providerInfo == null ? NULL : providerInfo.toJson());
            jsonObject.put(NAME_ORIGINAL_JSON, new JSONObject(originalJson));
        } catch (JSONException exception) {
            OPFLog.e("", exception);
        }
        return jsonObject;
    }

    @Override
    public String toString() {
        return OPFIabUtils.toString(this);
    }

    abstract static class Builder {

        @NonNull
        protected final String sku;
        @Nullable
        protected SkuType type;
        @Nullable
        protected BillingProviderInfo providerInfo;
        @Nullable
        protected String originalJson;

        protected Builder(@NonNull final String sku) {
            this.sku = sku;
        }

        protected Builder setType(@Nullable final SkuType type) {
            this.type = type;
            return this;
        }

        protected Builder setProviderInfo(@Nullable final BillingProviderInfo providerInfo) {
            this.providerInfo = providerInfo;
            return this;
        }

        protected Builder setOriginalJson(@Nullable final String originalJson) {
            this.originalJson = originalJson;
            return this;
        }

        protected Builder setBillingModel(@NonNull final BillingModel billingModel) {
            setType(billingModel.getType());
            setOriginalJson(billingModel.getOriginalJson());
            setProviderInfo(billingModel.getProviderInfo());
            return this;
        }

        public abstract BillingModel build();
    }
}
