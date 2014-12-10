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

public abstract class Purchase extends BillingModel {

    @NonNull
    private final SkuDetails skuDetails;

    Purchase(@NonNull final SkuDetails skuDetails, @Nullable final String json) {
        super(json);
        this.skuDetails = skuDetails;
    }

    @NonNull
    public SkuDetails getSkuDetails() {
        return skuDetails;
    }

    @NonNull
    public SkuDetails.Type getType() {
        return skuDetails.getType();
    }
}
