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

package org.onepf.opfiab.model.event.billing;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;
import org.onepf.opfiab.model.BillingProviderInfo;
import org.onepf.opfutils.OPFLog;

import java.util.Arrays;
import java.util.Collection;

import static org.json.JSONObject.NULL;
import static org.onepf.opfiab.model.event.billing.Status.PENDING;
import static org.onepf.opfiab.model.event.billing.Status.SUCCESS;

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

    @Nullable
    public BillingProviderInfo getProviderInfo() {
        return providerInfo;
    }

    @NonNull
    public Status getStatus() {
        return status;
    }

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
    @SuppressWarnings("PMD")
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
