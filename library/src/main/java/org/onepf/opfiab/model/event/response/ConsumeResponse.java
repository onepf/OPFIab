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

import org.onepf.opfiab.model.billing.ConsumableDetails;

public class ConsumeResponse extends Response {

    @NonNull
    private final ConsumableDetails consumableDetails;

    public ConsumeResponse(@NonNull final Status status, @NonNull final ConsumableDetails consumableDetails) {
        super(Type.CONSUME, status);
        this.consumableDetails = consumableDetails;
    }

    @NonNull
    public ConsumableDetails getConsumableDetails() {
        return consumableDetails;
    }
}
