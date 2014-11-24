/*
 * Copyright 2012-2014 One Platform Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.onepf.opfiab.model.response;

import android.os.Bundle;
import android.support.annotation.NonNull;

import org.onepf.opfiab.OPFIabAction;
import org.onepf.opfiab.billing.ResponseStatus;

import java.io.Serializable;

public abstract class Response implements Serializable {

    @NonNull
    private final OPFIabAction action;

    @NonNull
    private final Bundle data;

    @NonNull
    private final ResponseStatus status;

    Response(@NonNull final OPFIabAction action,
             @NonNull final ResponseStatus status,
             @NonNull final Bundle data) {
        this.action = action;
        this.status = status;
        this.data = data;
    }

    @NonNull
    public OPFIabAction getAction() {
        return action;
    }

    @NonNull
    public Bundle getData() {
        return data;
    }

    @NonNull
    public ResponseStatus getStatus() {
        return status;
    }
}