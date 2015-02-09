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

public class SkuDetails extends BillingModel {

    @Nullable
    private final String price;
    @Nullable
    private final String title;
    @Nullable
    private final String description;
    @Nullable
    private final String iconUrl;

    protected SkuDetails(@NonNull final String sku,
                         @Nullable final SkuType type,
                         @Nullable final String json,
                         @Nullable final String price,
                         @Nullable final String title,
                         @Nullable final String description,
                         @Nullable final String iconUrl) {
        super(sku, type, json);
        this.price = price;
        this.title = title;
        this.description = description;
        this.iconUrl = iconUrl;
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


    public static class Builder extends BillingModel.Builder {

        @Nullable
        private String price = null;
        @Nullable
        private String title = null;
        @Nullable
        private String description = null;
        @Nullable
        private String iconUrl = null;

        public Builder(@NonNull final String sku) {
            super(sku);
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

        public SkuDetails build() {
            return new SkuDetails(sku, type, json, price, title, description, iconUrl);
        }
    }
}
