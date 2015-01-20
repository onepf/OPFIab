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

import org.onepf.opfiab.model.event.BillingEvent;
import org.onepf.opfiab.model.event.request.Request;

public abstract class Response extends BillingEvent {

    public enum Status {

        SUCCESS,
        PENDING,
        USER_CANCELED,
        BILLING_UNAVAILABLE,
        ITEM_UNAVAILABLE,
        ITEM_ALREADY_OWNED,
        SUBSCRIPTIONS_NOT_SUPPORTED,
        UNAUTHORISED,
        UNKNOWN_ERROR,
    }

    @NonNull
    private final Status status;

    @NonNull
    private final Request request;

    protected Response(@NonNull final Request request,
                       @NonNull final Status status) {
        super(request.getType());
        this.request = request;
        this.status = status;
    }

    @NonNull
    public Request getRequest() {
        return request;
    }

    @Override
    public int getId() {
        return request.getId();
    }

    @NonNull
    public Status getStatus() {
        return status;
    }
}
