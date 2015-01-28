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

package org.onepf.opfiab.model.event;

import android.support.annotation.NonNull;

import com.google.gson.Gson;

import java.io.Serializable;

public abstract class BillingEvent implements Serializable {

    private static final Gson GSON = new Gson();

    public static <T extends BillingEvent> T fromJson(@NonNull final String json,
                                                      @NonNull final Class<T> clazz) {
        return GSON.fromJson(json, clazz);
    }

    public static enum Type {

        CONSUME,
        PURCHASE,
        SKU_DETAILS,
        INVENTORY,
    }


    @NonNull
    private final Type type;

    protected BillingEvent(@NonNull final Type type) {
        this.type = type;
    }

    @NonNull
    public Type getType() {
        return type;
    }

    @NonNull
    public String toJson() {
        return GSON.toJson(this);
    }
}
