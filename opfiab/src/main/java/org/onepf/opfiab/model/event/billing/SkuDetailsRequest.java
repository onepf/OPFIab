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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.onepf.opfiab.billing.BillingProvider;
import org.onepf.opfutils.OPFLog;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Request for {@link BillingProvider} to load details for corresponding SKUs.
 *
 * @see SkuDetailsResponse
 */
public class SkuDetailsRequest extends BillingRequest {

    private static final String NAME_SKUS = "skus";

    // Must use serializable Set
    @SuppressWarnings("PMD.LooseCoupling")
    @NonNull
    private final HashSet<String> skus = new LinkedHashSet<>();

    public SkuDetailsRequest(@NonNull final Set<String> skus) {
        this(null, false, skus);
    }

    @SuppressWarnings("TypeMayBeWeakened")
    public SkuDetailsRequest(@Nullable final Activity activity,
                                final boolean activityHandlesResult,
                                @NonNull final Set<String> skus) {
        super(BillingEventType.SKU_DETAILS, activity, activityHandlesResult);
        this.skus.addAll(skus);
    }

    /**
     * Gets SKUs to load details for.
     *
     * @return SKUs.
     */
    @NonNull
    public Set<String> getSkus() {
        return Collections.unmodifiableSet(skus);
    }

    @NonNull
    @Override
    public JSONObject toJson() {
        final JSONObject jsonObject = super.toJson();
        try {
            jsonObject.put(NAME_SKUS, new JSONArray(skus));
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

        final SkuDetailsRequest that = (SkuDetailsRequest) o;

        if (!skus.equals(that.skus)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + skus.hashCode();
        return result;
    }
    //CHECKSTYLE:ON
}
