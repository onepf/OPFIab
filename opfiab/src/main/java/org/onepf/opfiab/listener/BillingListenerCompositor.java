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

import org.onepf.opfiab.model.event.SetupResponse;
import org.onepf.opfiab.model.event.SetupStartedEvent;
import org.onepf.opfiab.model.event.billing.BillingRequest;
import org.onepf.opfiab.model.event.billing.BillingResponse;
import org.onepf.opfiab.model.event.billing.ConsumeResponse;
import org.onepf.opfiab.model.event.billing.InventoryResponse;
import org.onepf.opfiab.model.event.billing.PurchaseResponse;
import org.onepf.opfiab.model.event.billing.SkuDetailsResponse;

import java.util.Collection;
import java.util.HashSet;

/**
 * Handy implementation of {@link BillingListener} interface which aggregates multiple listener
 * objects.
 */
public class BillingListenerCompositor implements BillingListener {

    protected final Collection<BillingListener> billingListeners = new HashSet<>();

    protected final Collection<OnSetupListener> setupListeners = new HashSet<>();
    protected final Collection<OnPurchaseListener> purchaseListeners = new HashSet<>();
    protected final Collection<OnInventoryListener> inventoryListeners = new HashSet<>();
    protected final Collection<OnSkuDetailsListener> skuDetailsListeners = new HashSet<>();
    protected final Collection<OnConsumeListener> consumeListeners = new HashSet<>();

    public BillingListenerCompositor() {
        super();
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

    public void addSkuDetailsListener(@NonNull final OnSkuDetailsListener skuInfoListener) {
        skuDetailsListeners.add(skuInfoListener);
    }

    public void addConsumeListener(@NonNull final OnConsumeListener consumeListener) {
        consumeListeners.add(consumeListener);
    }

    public void addBillingListener(@NonNull final BillingListener billingListener) {
        billingListeners.add(billingListener);

        addSetupListener(billingListener);
        addPurchaseListener(billingListener);
        addInventoryListener(billingListener);
        addSkuDetailsListener(billingListener);
        addConsumeListener(billingListener);
    }

    @Override
    public void onRequest(@NonNull final BillingRequest billingRequest) {
        for (final BillingListener billingListener : billingListeners) {
            billingListener.onRequest(billingRequest);
        }
    }

    @Override
    public void onResponse(@NonNull final BillingResponse billingResponse) {
        for (final BillingListener billingListener : billingListeners) {
            billingListener.onResponse(billingResponse);
        }
    }

    @Override
    public void onSetupStarted(@NonNull final SetupStartedEvent setupStartedEvent) {
        for (final OnSetupListener setupListener : setupListeners) {
            setupListener.onSetupStarted(setupStartedEvent);
        }
    }

    @Override
    public void onSetupResponse(@NonNull final SetupResponse setupResponse) {
        for (final OnSetupListener setupListener : setupListeners) {
            setupListener.onSetupResponse(setupResponse);
        }
    }

    @Override
    public void onPurchase(@NonNull final PurchaseResponse purchaseResponse) {
        for (final OnPurchaseListener purchaseListener : purchaseListeners) {
            purchaseListener.onPurchase(purchaseResponse);
        }
    }

    @Override
    public void onConsume(@NonNull final ConsumeResponse consumeResponse) {
        for (final OnConsumeListener consumeListener : consumeListeners) {
            consumeListener.onConsume(consumeResponse);
        }
    }

    @Override
    public void onInventory(@NonNull final InventoryResponse inventoryResponse) {
        for (final OnInventoryListener inventoryListener : inventoryListeners) {
            inventoryListener.onInventory(inventoryResponse);
        }
    }

    @Override
    public void onSkuDetails(@NonNull final SkuDetailsResponse skuDetailsResponse) {
        for (final OnSkuDetailsListener skuDetailsListener : skuDetailsListeners) {
            skuDetailsListener.onSkuDetails(skuDetailsResponse);
        }
    }
}
