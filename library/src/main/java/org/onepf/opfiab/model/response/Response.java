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

package org.onepf.opfiab.model.response;

import android.support.annotation.NonNull;

import java.io.Serializable;

public abstract class Response implements Serializable {

    public static enum Type {
        CONSUME,
        PURCHASE,
        SKU_INFO,
        INVENTORY,
    }

    public enum Status {
        SUCCESS,
        PENDING,
        USER_CANCELED,
        BILLING_UNAVAILABLE,
        ITEM_UNAVAILABLE,
        ITEM_ALREADY_OWNED,
        SUBSCRIPTIONS_NOT_SUPPORTED,
    }


    @NonNull
    private final Type type;

    @NonNull
    private final Status status;

    Response(@NonNull final Type type,
             @NonNull final Status status) {
        this.type = type;
        this.status = status;
    }

    @NonNull
    public Type getType() {
        return type;
    }

    @NonNull
    public Status getStatus() {
        return status;
    }
}
