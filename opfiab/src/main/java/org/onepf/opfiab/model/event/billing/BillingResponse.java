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

import org.onepf.opfiab.model.BillingProviderInfo;
import org.onepf.opfiab.model.event.BillingEvent;

import java.util.Arrays;
import java.util.Collection;

import static org.onepf.opfiab.model.event.billing.BillingResponse.Status.PENDING;
import static org.onepf.opfiab.model.event.billing.BillingResponse.Status.SUCCESS;

public abstract class BillingResponse extends BillingEvent {

    public static enum Status {

        /**
         * Everything is OK.
         */
        SUCCESS,
        /**
         * Request was handled, but takes considerable time to process.
         */
        PENDING,
        /**
         * Billing provider requires authorisation.
         */
        UNAUTHORISED,
        /**
         * Library is busy with another request.
         */
        BUSY,
        /**
         * User canceled billing request.
         */
        USER_CANCELED,
        /**
         * Billing provider reported it can't handle billing.
         */
        BILLING_UNAVAILABLE,
        /**
         * Library has no working billing provider.
         */
        NO_BILLING_PROVIDER,
        /**
         * Request can't be handled at a time.<br>
         * For example: connection is down.
         */
        SERVICE_UNAVAILABLE,
        /**
         * Requested sku is unavailable from current billing provider.
         */
        ITEM_UNAVAILABLE,
        /**
         * Item is already owned by user.<br>
         * If it's consumable - consume() must be called.
         */
        ITEM_ALREADY_OWNED,
        /**
         * For some reason billing provider refused to handle request.
         */
        UNKNOWN_ERROR,
    }

    private static final Collection<Status> SUCCESSFUL = Arrays.asList(SUCCESS, PENDING);


    @Nullable
    private final BillingProviderInfo providerInfo;
    @NonNull
    private final Status status;

    protected BillingResponse(@Nullable final BillingProviderInfo providerInfo,
                              @NonNull final Type type,
                              @NonNull final Status status) {
        super(type);
        this.providerInfo = providerInfo;
        this.status = status;
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

    //CHECKSTYLE:OFF
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
