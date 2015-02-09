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

public class Purchase extends BillingModel {

    @Nullable
    private final String token;
    private final long purchaseTime;
    private final boolean canceled;

    protected Purchase(@NonNull final String sku,
                       @Nullable final SkuType type,
                       @Nullable final String json,
                       @Nullable final String token,
                       final long purchaseTime,
                       final boolean canceled) {
        super(sku, type, json);
        this.token = token;
        this.purchaseTime = purchaseTime;
        this.canceled = canceled;
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

    public static class Builder extends BillingModel.Builder {

        @Nullable
        private String token = null;
        private long purchaseTime = 0L;
        private boolean canceled = false;

        public Builder(@NonNull final String sku) {
            super(sku);
        }

        public void setToken(@Nullable final String token) {
            this.token = token;
        }

        public void setPurchaseTime(final long purchaseTime) {
            this.purchaseTime = purchaseTime;
        }

        public void setCanceled(final boolean canceled) {
            this.canceled = canceled;
        }

        @Override
        public Purchase build() {
            return new Purchase(sku, type, json, token, purchaseTime, canceled);
        }
    }
}
