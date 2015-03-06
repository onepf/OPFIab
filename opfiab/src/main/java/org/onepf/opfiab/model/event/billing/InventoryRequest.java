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
import org.onepf.opfutils.OPFLog;

public class InventoryRequest extends BillingRequest {

    private static final String NAME_START_OVER = "start_over";


    private final boolean startOver;

    public InventoryRequest(final boolean startOver) {
        super(Type.INVENTORY);
        this.startOver = startOver;
    }

    public boolean startOver() {
        return startOver;
    }

    @NonNull
    @Override
    public JSONObject toJson() {
        final JSONObject jsonObject = super.toJson();
        try {
            jsonObject.put(NAME_START_OVER, startOver);
        } catch (JSONException exception) {
            OPFLog.e("", exception);
        }
        return jsonObject;
    }

    //CHECKSTYLE:OFF
    @SuppressWarnings({"PMD", "RedundantIfStatement"})
    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        final InventoryRequest that = (InventoryRequest) o;

        if (startOver != that.startOver) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (startOver ? 1 : 0);
        return result;
    }
    //CHECKSTYLE:ON
}
