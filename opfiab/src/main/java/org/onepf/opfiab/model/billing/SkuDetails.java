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
import android.text.TextUtils;

import org.json.JSONException;
import org.json.JSONObject;
import org.onepf.opfiab.billing.BillingProvider;
import org.onepf.opfutils.OPFLog;

import static org.json.JSONObject.NULL;

/**
 * Model class representing detailed data of SKU available for purchase.
 */
public class SkuDetails extends BillingModel {

    private static final String NAME_PRICE = "price";
    private static final String NAME_TITLE = "title";
    private static final String NAME_DESCRIPTION = "description";


    @Nullable
    private final String price;
    @Nullable
    private final String title;
    @Nullable
    private final String description;

    @SuppressWarnings({"checkstyle:parameternumber"})
    protected SkuDetails(@NonNull final String sku,
                         @Nullable final SkuType type,
                         @Nullable final String providerName,
                         @Nullable final String originalJson,
                         @Nullable final String price,
                         @Nullable final String title,
                         @Nullable final String description) {
        super(sku, type, providerName, originalJson);
        this.price = price;
        this.title = title;
        this.description = description;
    }

    public SkuDetails(@NonNull final String sku) {
        this(sku, null, null, null, null, null, null);
    }

    /**
     * Gets formatted, localized price for the SKU.
     *
     * @return SKU price. Can be null.
     */
    @Nullable
    public String getPrice() {
        return price;
    }

    /**
     * Gets SKU title.
     *
     * @return SKU title. Can be null.
     */
    @Nullable
    public String getTitle() {
        return title;
    }

    /**
     * Gets localized description for SKU.
     *
     * @return SKU description. Can be null.
     */
    @Nullable
    public String getDescription() {
        return description;
    }

    /**
     * Indicates if there's actually any data available in this object.
     * <br>
     * Typically this is true when SKU wasn't recognized by {@link BillingProvider}.
     *
     * @return True is there's no data in this object. False otherwise.
     */
    public boolean isEmpty() {
        return TextUtils.isEmpty(price)
                && TextUtils.isEmpty(title)
                && TextUtils.isEmpty(description);
    }

    @NonNull
    @Override
    public SkuDetails copyWithSku(@NonNull final String sku) {
        return new Builder(sku).setSkuDetails(this).build();
    }

    @SuppressWarnings("PMD.NPathComplexity")
    @NonNull
    @Override
    public JSONObject toJson() {
        final JSONObject jsonObject = super.toJson();
        try {
            jsonObject.put(NAME_PRICE, price == null ? NULL : price);
            jsonObject.put(NAME_TITLE, title == null ? NULL : title);
            jsonObject.put(NAME_DESCRIPTION, description == null ? NULL : description);
        } catch (JSONException exception) {
            OPFLog.e("", exception);
        }
        return jsonObject;
    }

    /**
     * Builder class for {@link SkuDetails} object.
     */
    public static class Builder extends BillingModel.Builder {

        @Nullable
        protected String price;
        @Nullable
        protected String title;
        @Nullable
        protected String description;

        public Builder(@NonNull final String sku) {
            super(sku);
        }

        @Override
        public Builder setType(@Nullable final SkuType type) {
            return (Builder) super.setType(type);
        }

        @Override
        public Builder setProviderName(@Nullable final String providerName) {
            return (Builder) super.setProviderName(providerName);
        }

        @Override
        public Builder setOriginalJson(@Nullable final String originalJson) {
            return (Builder) super.setOriginalJson(originalJson);
        }

        @Override
        public Builder setBillingModel(@NonNull final BillingModel billingModel) {
            return (Builder) super.setBillingModel(billingModel);
        }

        /**
         * Sets price for a new SkuDetails object.
         *
         * @param price Formatted, localized price.
         * @return this object.
         */
        public Builder setPrice(@Nullable final String price) {
            this.price = price;
            return this;
        }

        /**
         * Sets title for a new SkuDetails object.
         *
         * @param title SKU title.
         * @return this object.
         */
        public Builder setTitle(@Nullable final String title) {
            this.title = title;
            return this;
        }

        /**
         * Sets localized description for a new SkuDetails object.
         *
         * @param description SKU description.
         * @return this object.
         */
        public Builder setDescription(@Nullable final String description) {
            this.description = description;
            return this;
        }

        /**
         * Sets existing SkuDetails object to use as a base for a new one.
         *
         * @param skuDetails Existing SkuDetails object.
         * @return this object.
         */
        public Builder setSkuDetails(@NonNull final SkuDetails skuDetails) {
            setBillingModel(skuDetails);
            setPrice(skuDetails.getPrice());
            setTitle(skuDetails.getTitle());
            setDescription(skuDetails.getDescription());
            return this;
        }

        public SkuDetails build() {
            return new SkuDetails(sku, type, providerName, originalJson, price, title, description);
        }
    }
}
