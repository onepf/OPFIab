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
import org.onepf.opfiab.model.BillingProviderInfo;
import org.onepf.opfiab.model.billing.BillingModel;
import org.onepf.opfutils.OPFLog;

import java.util.Arrays;
import java.util.Collection;

import static org.json.JSONObject.NULL;
import static org.onepf.opfiab.model.event.billing.Status.PENDING;
import static org.onepf.opfiab.model.event.billing.Status.SUCCESS;

/**
 * Model class representing response from {@link BillingProvider} for corresponding {@link BillingRequest}.
 */
public abstract class BillingResponse extends BillingEvent {

    private static final String NAME_PROVIDER_INFO = "provider_info";
    private static final String NAME_STATUS = "status";

    private static final Collection<Status> SUCCESSFUL = Arrays.asList(SUCCESS, PENDING);


    @Nullable
    private final BillingProviderInfo providerInfo;
    @NonNull
    private final Status status;

    protected BillingResponse(@NonNull final Type type,
                              @NonNull final Status status,
                              @Nullable final BillingProviderInfo providerInfo) {
        super(type);
        this.status = status;
        this.providerInfo = providerInfo;
    }

    /**
     * Get information about {@link BillingProvider} which is responsible for this BillingResponse.
     * <br>
     * Might be useful to properly handle some data from {@link BillingModel#getOriginalJson()}.
     *
     * @return BillingProviderInfo object from corresponding {@link BillingProvider}.
     */
    @Nullable
    public BillingProviderInfo getProviderInfo() {
        return providerInfo;
    }

    /**
     * Indicates whether corresponding billing operation was successful or what kind of error caused
     * it to fail.
     *
     * @return Status of this BillingResponse.
     */
    @NonNull
    public Status getStatus() {
        return status;
    }

    /**
     * Indicates whether status of this response is successful.
     *
     * @return True if this BillingResponse was not caused by any kind of error, false otherwise.
     */
    public boolean isSuccessful() {
        return SUCCESSFUL.contains(getStatus());
    }

    @NonNull
    @Override
    public JSONObject toJson() {
        final JSONObject jsonObject = super.toJson();
        try {
            jsonObject.put(NAME_STATUS, status);
            jsonObject.put(NAME_PROVIDER_INFO, providerInfo == null ? NULL : providerInfo.toJson());
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

        final BillingResponse that = (BillingResponse) o;

        if (providerInfo != null ? !providerInfo.equals(
                that.providerInfo) : that.providerInfo != null)
            return false;
        if (status != that.status) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (providerInfo != null ? providerInfo.hashCode() : 0);
        result = 31 * result + status.hashCode();
        return result;
    }
    //CHECKSTYLE:ON
}
