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

package org.onepf.opfiab.model.event.response;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.onepf.opfiab.model.BillingProviderInfo;
import org.onepf.opfiab.model.event.BillingEvent;
import org.onepf.opfiab.model.event.request.Request;

import java.util.Arrays;
import java.util.Collection;

import static org.onepf.opfiab.model.event.response.Response.Status.PENDING;
import static org.onepf.opfiab.model.event.response.Response.Status.SUCCESS;

public abstract class Response extends BillingEvent {

    public static enum Status {

        SUCCESS,
        PENDING,

        UNAUTHORISED,

        BUSY,
        USER_CANCELED,
        BILLING_UNAVAILABLE,
        ITEM_UNAVAILABLE,
        ITEM_ALREADY_OWNED,
        SUBSCRIPTIONS_NOT_SUPPORTED,
        UNKNOWN_ERROR,
    }

    private static final Collection<Status> SUCCESSFUL = Arrays.asList(SUCCESS, PENDING);


    @Nullable
    private final BillingProviderInfo providerInfo;
    @NonNull
    private final Request request;
    @NonNull
    private final Status status;

    protected Response(@Nullable final BillingProviderInfo providerInfo,
                       @NonNull final Type type,
                       @NonNull final Request request,
                       @NonNull final Status status) {
        super(type);
        if (request.getType() != type) {
            throw new IllegalArgumentException("Request and Response types doesn't match.");
        }
        this.providerInfo = providerInfo;
        this.request = request;
        this.status = status;
    }

    @Nullable
    public BillingProviderInfo getProviderInfo() {
        return providerInfo;
    }

    @NonNull
    public Request getRequest() {
        return request;
    }

    @NonNull
    public Status getStatus() {
        return status;
    }

    public boolean isSuccessful() {
        return SUCCESSFUL.contains(getStatus());
    }
}
