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
import org.onepf.opfiab.model.BillingProviderInfo;
import org.onepf.opfutils.OPFLog;

import static org.json.JSONObject.NULL;

/**
 * Model class representing detailed data of SKU available for purchase.
 */
public class SkuDetails extends BillingModel {

    private static final String NAME_PRICE = "price";
    private static final String NAME_TITLE = "title";
    private static final String NAME_DESCRIPTION = "description";
    private static final String NAME_ICON_URL = "iconUrl";


    @Nullable
    private final String price;
    @Nullable
    private final String title;
    @Nullable
    private final String description;
    @Nullable
    private final String iconUrl;

    @SuppressWarnings({"checkstyle:parameternumber"})
    protected SkuDetails(@NonNull final String sku,
                         @Nullable final SkuType type,
                         @Nullable final BillingProviderInfo providerInfo,
                         @Nullable final String originalJson,
                         @Nullable final String price,
                         @Nullable final String title,
                         @Nullable final String description,
                         @Nullable final String iconUrl) {
        super(sku, type, providerInfo, originalJson);
        this.price = price;
        this.title = title;
        this.description = description;
        this.iconUrl = iconUrl;
    }

    public SkuDetails(@NonNull final String sku) {
        this(sku, null, null, null, null, null, null, null);
    }

    /**
     * Get formatted, localized price for SKU.
     *
     * @return SKU price. Can be null.
     */
    @Nullable
    public String getPrice() {
        return price;
    }

    /**
     * Get SKU title.
     *
     * @return SKU title. Can be null.
     */
    @Nullable
    public String getTitle() {
        return title;
    }

    /**
     * Get localized description for SKU.
     *
     * @return SKU description. Can be null.
     */
    @Nullable
    public String getDescription() {
        return description;
    }

    /**
     * Get URL of the image associated with SKU.
     *
     * @return URL for SKU image. Can be null.
     */
    @Nullable
    public String getIconUrl() {
        return iconUrl;
    }

    /**
     * Indicates whether this there's actually any data available in this object.
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

    @SuppressWarnings("PMD.NPathComplexity")
    @NonNull
    @Override
    public JSONObject toJson() {
        final JSONObject jsonObject = super.toJson();
        try {
            jsonObject.put(NAME_PRICE, price == null ? NULL : price);
            jsonObject.put(NAME_TITLE, title == null ? NULL : title);
            jsonObject.put(NAME_DESCRIPTION, description == null ? NULL : description);
            jsonObject.put(NAME_ICON_URL, iconUrl == null ? NULL : iconUrl);
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
        private String price;
        @Nullable
        private String title;
        @Nullable
        private String description;
        @Nullable
        private String iconUrl;

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

        /**
         * Set price for new SkuDetails object.
         *
         * @param price Formatted, localized price.
         * @return this object.
         */
        public Builder setPrice(@Nullable final String price) {
            this.price = price;
            return this;
        }

        /**
         * Set title for new SkuDetails object.
         *
         * @param title SKU title.
         * @return this object.
         */
        public Builder setTitle(@Nullable final String title) {
            this.title = title;
            return this;
        }

        /**
         * Set localized description for new SkuDetails object.
         *
         * @param description SKU description.
         * @return this object.
         */
        public Builder setDescription(@Nullable final String description) {
            this.description = description;
            return this;
        }

        /**
         * Set image URL for new SkuDetails object.
         *
         * @param iconUrl SKU image URL.
         * @return this object.
         */
        public Builder setIconUrl(@Nullable final String iconUrl) {
            this.iconUrl = iconUrl;
            return this;
        }

        /**
         * Set existing SkuDetails object to use as a base for new one.
         *
         * @param skuDetails Existing SkuDetails object.
         * @return this object.
         */
        public Builder setSkuDetails(@NonNull final SkuDetails skuDetails) {
            setBillingModel(skuDetails);
            setPrice(skuDetails.getPrice());
            setTitle(skuDetails.getTitle());
            setDescription(skuDetails.getDescription());
            setIconUrl(skuDetails.getIconUrl());
            return this;
        }

        public SkuDetails build() {
            return new SkuDetails(sku, type, providerInfo, originalJson, price, title, description,
                                  iconUrl);
        }
    }
}
