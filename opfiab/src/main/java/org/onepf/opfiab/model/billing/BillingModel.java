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
import org.onepf.opfiab.billing.BillingProvider;
import org.onepf.opfiab.model.BillingProviderInfo;
import org.onepf.opfiab.model.JsonCompatible;
import org.onepf.opfiab.util.OPFIabUtils;
import org.onepf.opfutils.OPFLog;

import java.io.Serializable;

import static org.json.JSONObject.NULL;

/**
 * Parent class for all billing models.
 */
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

    /**
     * Get Store Keeping Unit associated with this billing model.
     *
     * @return SKU associated with this billing model.
     */
    @NonNull
    public String getSku() {
        return sku;
    }

    /**
     * Get type of this billing model.
     *
     * @return Type of this billing model.
     */
    @NonNull
    public SkuType getType() {
        return type;
    }

    /**
     * Get info of {@link BillingProvider} associated with this billing model.
     *
     * @return BillingProviderInfo associated with this billing model.
     */
    @Nullable
    public BillingProviderInfo getProviderInfo() {
        return providerInfo;
    }

    /**
     * Get JSON representation of data from which this billing model was constructed.
     *
     * @return JSON representation of data originally returned by {@link BillingProvider}.
     */
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
            jsonObject.put(NAME_ORIGINAL_JSON,
                           originalJson == null ? NULL : new JSONObject(originalJson));
        } catch (JSONException exception) {
            OPFLog.e("", exception);
        }
        return jsonObject;
    }

    @Override
    public String toString() {
        return OPFIabUtils.toString(this);
    }

    //CHECKSTYLE:OFF
    @SuppressWarnings({"PMD", "RedundantIfStatement"})
    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof BillingModel)) return false;

        final BillingModel that = (BillingModel) o;

        if (providerInfo != null ? !providerInfo.equals(
                that.providerInfo) : that.providerInfo != null)
            return false;
        if (!sku.equals(that.sku)) return false;
        if (type != that.type) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = sku.hashCode();
        result = 31 * result + type.hashCode();
        result = 31 * result + (providerInfo != null ? providerInfo.hashCode() : 0);
        return result;
    }
    //CHECKSTYLE:ON

    /**
     * Parent class for all billing models builders.
     */
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

        /**
         * Set type for new billing model.
         *
         * @param type Type to set.
         * @return this object.
         */
        protected Builder setType(@Nullable final SkuType type) {
            this.type = type;
            return this;
        }

        /**
         * Set billing provider info for new billing model.
         *
         * @param providerInfo BillingProviderInfo object to set.
         * @return this object.
         */
        protected Builder setProviderInfo(@Nullable final BillingProviderInfo providerInfo) {
            this.providerInfo = providerInfo;
            return this;
        }

        /**
         * Set JSON representation of original data for new billing model.
         *
         * @param originalJson JSON data to set.
         * @return this object.
         */
        protected Builder setOriginalJson(@Nullable final String originalJson) {
            this.originalJson = originalJson;
            return this;
        }

        /**
         * Existing billing model to use as base for new billing model.
         *
         * @param billingModel BillingModel object to copy data from.
         * @return this object.
         */
        protected Builder setBillingModel(@NonNull final BillingModel billingModel) {
            setType(billingModel.getType());
            setOriginalJson(billingModel.getOriginalJson());
            setProviderInfo(billingModel.getProviderInfo());
            return this;
        }

        /**
         * Construct new billing model object.
         *
         * @return new BillingModer from supplied data.
         */
        public abstract BillingModel build();
    }
}
