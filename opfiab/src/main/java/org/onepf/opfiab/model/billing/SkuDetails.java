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
import org.onepf.opfiab.model.BillingProviderInfo;
import org.onepf.opfutils.OPFLog;

import static org.json.JSONObject.NULL;

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

    @Nullable
    public String getPrice() {
        return price;
    }

    @Nullable
    public String getTitle() {
        return title;
    }

    @Nullable
    public String getDescription() {
        return description;
    }

    @Nullable
    public String getIconUrl() {
        return iconUrl;
    }

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

        public Builder setPrice(@Nullable final String price) {
            this.price = price;
            return this;
        }

        public Builder setTitle(@Nullable final String title) {
            this.title = title;
            return this;
        }

        public Builder setDescription(@Nullable final String description) {
            this.description = description;
            return this;
        }

        public Builder setIconUrl(@Nullable final String iconUrl) {
            this.iconUrl = iconUrl;
            return this;
        }

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
