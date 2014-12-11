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
import android.support.annotation.Nullable;

import org.onepf.opfiab.listener.BillingListener;
import org.onepf.opfiab.listener.BillingListenerWrapper;
import org.onepf.opfiab.listener.OnConsumeListener;
import org.onepf.opfiab.listener.OnInventoryListener;
import org.onepf.opfiab.listener.OnPurchaseListener;
import org.onepf.opfiab.listener.OnSetupListener;
import org.onepf.opfiab.listener.OnSkuDetailsListener;
import org.onepf.opfiab.model.event.SetupEvent;
import org.onepf.opfiab.model.event.response.ConsumeResponse;
import org.onepf.opfiab.model.event.response.InventoryResponse;
import org.onepf.opfiab.model.event.response.PurchaseResponse;
import org.onepf.opfiab.model.event.response.Response;
import org.onepf.opfiab.model.event.response.SkuDetailsResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

class GlobalBillingListener extends BillingListenerWrapper {

    @NonNull
    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalBillingListener.class);

    @NonNull
    private static final Set<Reference<ManagedIabHelper>> HELPERS_REFS = new HashSet<>();

    static void subscribe(@NonNull final ManagedIabHelper managedIabHelper) {
        OPFUtils.checkThread(true);
        HELPERS_REFS.add(new WeakReference<ManagedIabHelper>(managedIabHelper));
    }

    static void unsubscribe(@NonNull final ManagedIabHelper managedIabHelper) {
        OPFUtils.checkThread(true);
        for (final Reference<ManagedIabHelper> helperReference : HELPERS_REFS) {
            final ManagedIabHelper helper = helperReference.get();
            if (managedIabHelper == helper) {
                // Since we immediately break after this modification, it shouldn't interfere with Iterator
                HELPERS_REFS.remove(helperReference);
                break;
            }
        }
    }

    private static List<ManagedIabHelper> helpersList() {
        final List<ManagedIabHelper> helpersList = new ArrayList<>();
        for (final Reference<ManagedIabHelper> helperReference : HELPERS_REFS) {
            final ManagedIabHelper helper;
            if ((helper = helperReference.get()) != null) {
                helpersList.add(helper);
            }
        }
        return Collections.unmodifiableList(helpersList);
    }


    GlobalBillingListener(@Nullable final BillingListener billingListener) {
        super(billingListener);
        OPFIab.getEventBus().register(this);
    }

    public void onEventMainThread(@NonNull final SetupEvent setupEvent) {
        onSetup(setupEvent);
    }

    public void onEventMainThread(@NonNull final Response response) {
        switch (response.getType()) {
            case PURCHASE:
                onPurchase((PurchaseResponse) response);
                break;
            case CONSUME:
                onConsume((ConsumeResponse) response);
                break;
            case INVENTORY:
                onInventory((InventoryResponse) response);
                break;
            case SKU_INFO:
                onSkuInfo((SkuDetailsResponse) response);
                break;
            default:
                LOGGER.error("Unknown Response event: ", response);
        }
    }

    @Override
    public void onSetup(@NonNull final SetupEvent setupEvent) {
        super.onSetup(setupEvent);
        for (final ManagedIabHelper helper : helpersList()) {
            final Collection<OnSetupListener> setupListeners =
                    new ArrayList<>(helper.setupListeners);
            for (final OnSetupListener setupListener : setupListeners) {
                setupListener.onSetup(setupEvent);
            }
        }
    }

    @Override
    public void onPurchase(@NonNull final PurchaseResponse purchaseResponse) {
        super.onPurchase(purchaseResponse);
        for (final ManagedIabHelper helper : helpersList()) {
            final Collection<OnPurchaseListener> purchaseListeners =
                    new ArrayList<>(helper.purchaseListeners);
            for (final OnPurchaseListener purchaseListener : purchaseListeners) {
                purchaseListener.onPurchase(purchaseResponse);
            }
        }
    }

    @Override
    public void onInventory(@NonNull final InventoryResponse inventoryResponse) {
        super.onInventory(inventoryResponse);
        for (final ManagedIabHelper helper : helpersList()) {
            final Collection<OnInventoryListener> inventoryListeners =
                    new ArrayList<>(helper.inventoryListeners);
            for (final OnInventoryListener inventoryListener : inventoryListeners) {
                inventoryListener.onInventory(inventoryResponse);
            }
        }
    }

    @Override
    public void onSkuInfo(@NonNull final SkuDetailsResponse skuDetailsResponse) {
        super.onSkuInfo(skuDetailsResponse);
        for (final ManagedIabHelper helper : helpersList()) {
            final Collection<OnSkuDetailsListener> skuInfoListeners =
                    new ArrayList<>(helper.skuInfoListeners);
            for (final OnSkuDetailsListener skuInfoListener : skuInfoListeners) {
                skuInfoListener.onSkuInfo(skuDetailsResponse);
            }
        }
    }

    @Override
    public void onConsume(@NonNull final ConsumeResponse consumeResponse) {
        super.onConsume(consumeResponse);
        for (final ManagedIabHelper helper : helpersList()) {
            final Collection<OnConsumeListener> consumeListeners =
                    new ArrayList<>(helper.consumeListeners);
            for (final OnConsumeListener consumeListener : consumeListeners) {
                consumeListener.onConsume(consumeResponse);
            }
        }
    }
}
