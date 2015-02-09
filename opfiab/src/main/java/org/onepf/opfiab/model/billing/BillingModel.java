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

import java.io.Serializable;

public abstract class BillingModel implements Serializable {

    @NonNull
    private final String sku;
    @NonNull
    private final SkuType type;
    @Nullable
    private final String json;

    protected BillingModel(@NonNull final String sku,
                 @Nullable final SkuType type,
                 @Nullable final String json) {
        this.sku = sku;
        this.type = type == null ? SkuType.UNKNOWN : type;
        this.json = json;
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
    public String getJson() {
        return json;
    }

    abstract static class Builder {

        @NonNull
        protected final String sku;
        @Nullable
        protected SkuType type = null;
        @Nullable
        protected String json = null;

        protected Builder(@NonNull final String sku) {
            this.sku = sku;
        }

        public Builder setType(@Nullable final SkuType type) {
            this.type = type;
            return this;
        }

        public Builder setJson(@Nullable final String json) {
            this.json = json;
            return this;
        }

        public abstract BillingModel build();
    }
}
