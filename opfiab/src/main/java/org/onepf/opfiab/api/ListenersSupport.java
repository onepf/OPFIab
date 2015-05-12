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

package org.onepf.opfiab.api;

import android.support.annotation.NonNull;

import org.onepf.opfiab.listener.BillingListener;
import org.onepf.opfiab.listener.OnConsumeListener;
import org.onepf.opfiab.listener.OnInventoryListener;
import org.onepf.opfiab.listener.OnPurchaseListener;
import org.onepf.opfiab.listener.OnSetupListener;
import org.onepf.opfiab.listener.OnSkuDetailsListener;
import org.onepf.opfiab.model.event.SetupResponse;

interface ListenersSupport {


    /**
     * Same as {@code addSetupListener(setupListener, true);}
     *
     * @see #addSetupListener(OnSetupListener, boolean)
     */
    void addSetupListener(@NonNull final OnSetupListener setupListener);

    /**
     * Registers callback for setup events with handy option to immediately receive last known
     * {@link SetupResponse}.
     *
     * @param setupListener Listener to register.
     * @param deliverLast   If true and setup was already finished, immediately deliver last {@link
     *                      SetupResponse}
     */
    void addSetupListener(@NonNull final OnSetupListener setupListener,
                          final boolean deliverLast);

    /**
     * Registers callback for purchase events.
     *
     * @param purchaseListener Listener to register.
     */
    void addPurchaseListener(@NonNull final OnPurchaseListener purchaseListener);

    /**
     * Registers callback for inventory events.
     *
     * @param inventoryListener Listener to register.
     */
    void addInventoryListener(@NonNull final OnInventoryListener inventoryListener);

    /**
     * Registers callback for SKU details events.
     *
     * @param skuInfoListener Listener to register.
     */
    void addSkuDetailsListener(@NonNull final OnSkuDetailsListener skuInfoListener);

    /**
     * Registers callback for consume events.
     *
     * @param consumeListener Listener to register.
     */
    void addConsumeListener(@NonNull final OnConsumeListener consumeListener);

    /**
     * Registers callback for all billing events.
     *
     * @param billingListener Listener to register.
     */
    void addBillingListener(@NonNull final BillingListener billingListener);
}
