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

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;
import org.onepf.opfiab.billing.BillingProvider;
import org.onepf.opfutils.OPFLog;

/**
 * Request for {@link BillingProvider} to load user inventory.
 * @see InventoryResponse
 */
public class InventoryRequest extends BillingRequest {

    private static final String NAME_START_OVER = "start_over";


    private final boolean startOver;

    public InventoryRequest(final boolean startOver) {
        this(null, false, startOver);
    }

    @SuppressWarnings("BooleanParameter")
    public InventoryRequest(@Nullable final Activity activity,
                            final boolean activityHandlesResult,
                            final boolean startOver) {
        super(BillingEventType.INVENTORY, activity, activityHandlesResult);
        this.startOver = startOver;
    }

    /**
     * Indicates whether user inventory should be loaded from the start, or continued from the point
     * of the last {@link BillingRequest}.
     *
     * @return True if inventory should be loaded from the start, false otherwise.
     */
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
