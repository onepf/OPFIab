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

package org.onepf.opfiab.model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;
import org.onepf.opfiab.model.billing.BillingModel;
import org.onepf.opfiab.util.OPFIabUtils;
import org.onepf.opfutils.OPFLog;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * Simple model class that represents java object created from JSON.
 */
public abstract class JsonModel implements JsonCompatible {

    @Nullable
    public static <E extends JsonModel> E fromOriginalJson(@NonNull final Class<E> clazz,
                                                           @NonNull final BillingModel model) {
        try {
            final Constructor<E> constructor = clazz.getConstructor(String.class);
            return constructor.newInstance(model.getOriginalJson());
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException
                | InstantiationException exception) {
            OPFLog.e("", exception);
        }
        return null;
    }


    @NonNull
    protected final JSONObject jsonObject;
    @NonNull
    private final String originalJson;

    public JsonModel(@NonNull final String originalJson)
            throws JSONException {
        this.jsonObject = new JSONObject(originalJson);
        this.originalJson = originalJson;
    }

    public JsonModel(@NonNull final JSONObject jsonObject) {
        this.jsonObject = jsonObject;
        this.originalJson = jsonObject.toString();
    }

    /**
     * Gets JSON data associated with this model.
     *
     * @return JSON string.
     */
    @NonNull
    public String getOriginalJson() {
        return originalJson;
    }

    @NonNull
    @Override
    public JSONObject toJson() {
        return jsonObject;
    }

    @Override
    public String toString() {
        return OPFIabUtils.toString(this);
    }
}
