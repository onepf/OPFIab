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

package org.onepf.opfiab.listener;


import android.support.annotation.NonNull;

import org.onepf.opfiab.billing.BillingProvider;
import org.onepf.opfiab.model.event.billing.BillingRequest;
import org.onepf.opfiab.model.event.billing.BillingResponse;

/**
 * Listener for all library events.
 */
public interface BillingListener
        extends OnSetupListener, OnPurchaseListener, OnConsumeListener, OnInventoryListener,
                OnSkuDetailsListener {

    /**
     * Called on every {@link BillingRequest} handled by library.
     *
     * @param billingRequest Request being handled.
     */
    void onRequest(@NonNull final BillingRequest billingRequest);

    /**
     * Called on every {@link BillingResponse} sent by {@link BillingProvider}.
     *
     * @param billingResponse Response being send.
     */
    void onResponse(@NonNull final BillingResponse billingResponse);
}
