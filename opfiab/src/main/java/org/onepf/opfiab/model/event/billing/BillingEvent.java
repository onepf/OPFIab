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

package org.onepf.opfiab.model.event.billing;

import android.support.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;
import org.onepf.opfiab.model.JsonCompatible;
import org.onepf.opfiab.util.OPFIabUtils;
import org.onepf.opfutils.OPFLog;

import java.io.Serializable;

/**
 * Model class representing some occurred billing event.
 */
abstract class BillingEvent implements JsonCompatible, Serializable {

    private static final String NAME_TYPE = "type";

    /**
     * Type of billing event.
     */
    public enum Type {

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

    /**
     * Get type of this event.
     *
     * @return Type of this event.
     */
    @NonNull
    public Type getType() {
        return type;
    }

    @NonNull
    @Override
    public JSONObject toJson() {
        final JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(NAME_TYPE, type);
        } catch (JSONException exception) {
            OPFLog.e("", exception);
        }
        return jsonObject;
    }

    @Override
    public String toString() {
        return OPFIabUtils.toString(this);
    }

    //CHECKSTYLE:OFF
    @SuppressWarnings({"PMD", "RedundantIfStatement"})
    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final BillingEvent event = (BillingEvent) o;

        if (type != event.type) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return type.hashCode();
    }
    //CHECKSTYLE:ON
}
