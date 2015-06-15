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
import android.support.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;
import org.onepf.opfiab.billing.BillingProvider;
import org.onepf.opfiab.model.billing.SkuDetails;
import org.onepf.opfutils.OPFLog;

import java.util.Collection;
import java.util.Collections;

import static org.json.JSONObject.NULL;

/**
 * Response from {@link BillingProvider} for corresponding {@link SkuDetailsRequest}.
 */
public class SkuDetailsResponse extends BillingResponse {

    private static final String NAME_SKUS_DETAILS = "skus_details";


    @Nullable
    private final Collection<SkuDetails> skusDetails;

    public SkuDetailsResponse(@NonNull final Status status,
                              @Nullable final String providerName,
                              @Nullable final Collection<SkuDetails> skusDetails) {
        super(BillingEventType.SKU_DETAILS, status, providerName);
        this.skusDetails = skusDetails == null
                ? null
                : Collections.unmodifiableCollection(skusDetails);
    }

    /**
     * Gets details for corresponding SKUs.
     * <p>
     * Some SKUs might not have been recognized by {@link BillingProvider} and are left empty.
     *
     * @return Collection of SkuDetails objects. Can be null.
     * @see #isSuccessful()
     * @see SkuDetails#isEmpty()
     */
    @Nullable
    public Collection<SkuDetails> getSkusDetails() {
        return skusDetails;
    }

    @NonNull
    @Override
    public JSONObject toJson() {
        final JSONObject jsonObject = super.toJson();
        try {
            if (skusDetails == null) {
                jsonObject.put(NAME_SKUS_DETAILS, NULL);
            } else {
                for (final SkuDetails skuDetails : skusDetails) {
                    jsonObject.accumulate(NAME_SKUS_DETAILS, skuDetails.toJson());
                }
            }
        } catch (JSONException exception) {
            OPFLog.e("", exception);
        }
        return jsonObject;
    }
}
