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

package org.onepf.opfiab;

import android.support.annotation.NonNull;

import org.onepf.opfiab.listener.BillingListener;
import org.onepf.opfiab.listener.OnConsumeListener;
import org.onepf.opfiab.listener.OnInventoryListener;
import org.onepf.opfiab.listener.OnPurchaseListener;
import org.onepf.opfiab.listener.OnSetupListener;
import org.onepf.opfiab.listener.OnSkuDetailsListener;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

// TODO: think about filter for listeners.
public class ManagedIabHelper extends IabHelperWrapper {

    @NonNull
    final Set<OnSetupListener> setupListeners = Collections.synchronizedSet(
            new HashSet<OnSetupListener>());

    @NonNull
    final Set<OnPurchaseListener> purchaseListeners = Collections.synchronizedSet(
            new HashSet<OnPurchaseListener>());

    @NonNull
    final Set<OnInventoryListener> inventoryListeners = Collections.synchronizedSet(
            new HashSet<OnInventoryListener>());

    @NonNull
    final Set<OnSkuDetailsListener> skuInfoListeners = Collections.synchronizedSet(
            new HashSet<OnSkuDetailsListener>());

    @NonNull
    final Set<OnConsumeListener> consumeListeners = Collections.synchronizedSet(
            new HashSet<OnConsumeListener>());

    @NonNull
    private final BaseIabHelper baseIabHelper;

    public ManagedIabHelper(@NonNull final BaseIabHelper baseIabHelper) {
        super(baseIabHelper);
        this.baseIabHelper = baseIabHelper;
    }

    public void addSetupListener(@NonNull final OnSetupListener setupListener) {
        setupListeners.add(setupListener);
    }

    public void addPurchaseListener(@NonNull final OnPurchaseListener purchaseListener) {
        purchaseListeners.add(purchaseListener);
    }

    public void addInventoryListener(@NonNull final OnInventoryListener inventoryListener) {
        inventoryListeners.add(inventoryListener);
    }

    public void addSkuInfoListener(@NonNull final OnSkuDetailsListener skuInfoListener) {
        skuInfoListeners.add(skuInfoListener);
    }

    public void addConsumeListener(@NonNull final OnConsumeListener consumeListener) {
        consumeListeners.add(consumeListener);
    }

    public void addBillingListener(@NonNull final BillingListener billingListener) {
        addSetupListener(billingListener);
        addPurchaseListener(billingListener);
        addInventoryListener(billingListener);
        addSkuInfoListener(billingListener);
        addConsumeListener(billingListener);
    }

    public void subscribe() {
        GlobalBillingListener.subscribe(this);
    }

    public void unsubscribe() {
        GlobalBillingListener.unsubscribe(this);
    }
}
