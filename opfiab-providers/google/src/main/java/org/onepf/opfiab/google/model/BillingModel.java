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

package org.onepf.opfiab.google.model;

import android.support.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

abstract class BillingModel {

    @NonNull
    protected final JSONObject jsonObject;
    @NonNull
    private final String originalJson;

    public BillingModel(@NonNull final String originalJson)
            throws JSONException {
        this.jsonObject = new JSONObject(originalJson);
        this.originalJson = originalJson;
    }

    /**
     * Gets JSON data associated with this billing model.
     *
     * @return JSON string.
     */
    @NonNull
    public String getOriginalJson() {
        return originalJson;
    }
}
