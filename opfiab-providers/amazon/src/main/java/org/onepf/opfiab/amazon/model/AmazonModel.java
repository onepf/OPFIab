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

package org.onepf.opfiab.amazon.model;

import android.support.annotation.NonNull;

import org.json.JSONException;
import org.onepf.opfiab.model.JsonModel;

public abstract class AmazonModel extends JsonModel {

    protected static final String NAME_SKU = "sku";

    @NonNull
    protected final String sku;

    public AmazonModel(@NonNull final String originalJson) throws JSONException {
        super(originalJson);
        this.sku = jsonObject.getString(NAME_SKU);
    }

    @NonNull
    public abstract String getSku();
}
