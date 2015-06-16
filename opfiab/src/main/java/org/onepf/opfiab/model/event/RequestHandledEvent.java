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

package org.onepf.opfiab.model.event;

import android.support.annotation.NonNull;

import org.onepf.opfiab.billing.BillingProvider;
import org.onepf.opfiab.model.event.billing.BillingRequest;

/**
 * Class intended to indicate that {@link BillingProvider} finished handling {@link BillingRequest}.
 * <p>
 * Intended for internal usage.
 *
 * @see BillingProvider#onEventAsync(BillingRequest)
 */
public class RequestHandledEvent {

    @NonNull
    private final BillingRequest billingRequest;

    public RequestHandledEvent(@NonNull final BillingRequest billingRequest) {
        this.billingRequest = billingRequest;
    }

    /**
     * Gets request that was handled by {@link BillingProvider}.
     *
     * @return BillingRequest object.
     */
    @NonNull
    public BillingRequest getBillingRequest() {
        return billingRequest;
    }
}
