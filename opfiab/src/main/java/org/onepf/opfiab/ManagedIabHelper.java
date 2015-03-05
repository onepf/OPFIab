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
import org.onepf.opfiab.model.event.SetupResponse;
import org.onepf.opfiab.model.event.billing.BillingResponse;
import org.onepf.opfiab.model.event.billing.ConsumeResponse;
import org.onepf.opfiab.model.event.billing.InventoryResponse;
import org.onepf.opfiab.model.event.billing.PurchaseResponse;
import org.onepf.opfiab.model.event.billing.SkuDetailsResponse;
import org.onepf.opfutils.OPFChecks;

import java.util.HashSet;
import java.util.Set;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

public class ManagedIabHelper extends ScheduledIabHelper {

    @NonNull
    private final Set<OnSetupListener> setupListeners = new HashSet<>();
    @NonNull
    private final Set<OnPurchaseListener> purchaseListeners = new HashSet<>();
    @NonNull
    private final Set<OnInventoryListener> inventoryListeners = new HashSet<>();
    @NonNull
    private final Set<OnSkuDetailsListener> skuDetailsListeners = new HashSet<>();
    @NonNull
    private final Set<OnConsumeListener> consumeListeners = new HashSet<>();

    ManagedIabHelper(@NonNull final BaseIabHelper baseIabHelper) {
        super(baseIabHelper);
    }

    @SuppressFBWarnings({"BC_UNCONFIRMED_CAST"})
    public void onEventMainThread(@NonNull final BillingResponse event) {
        switch (event.getType()) {
            case CONSUME:
                for (final OnConsumeListener listener : consumeListeners) {
                    listener.onConsume((ConsumeResponse) event);
                }
                break;
            case PURCHASE:
                for (final OnPurchaseListener listener : purchaseListeners) {
                    listener.onPurchase((PurchaseResponse) event);
                }
                break;
            case SKU_DETAILS:
                for (final OnSkuDetailsListener listener : skuDetailsListeners) {
                    listener.onSkuDetails((SkuDetailsResponse) event);
                }
                break;
            case INVENTORY:
                for (final OnInventoryListener listener : inventoryListeners) {
                    listener.onInventory((InventoryResponse) event);
                }
                break;
        }
    }

    public void onEventMainThread(@NonNull final SetupResponse event) {
        for (final OnSetupListener listener : setupListeners) {
            listener.onSetup(event);
        }
    }

    public void addSetupListener(@NonNull final OnSetupListener setupListener) {
        OPFChecks.checkThread(true);
        setupListeners.add(setupListener);
        // Deliver last setup even right away
        final SetupResponse setupResponse = OPFIab.getBaseHelper().getSetupResponse();
        if (setupResponse != null) {
            setupListener.onSetup(setupResponse);
        }
    }

    public void addPurchaseListener(@NonNull final OnPurchaseListener purchaseListener) {
        OPFChecks.checkThread(true);
        purchaseListeners.add(purchaseListener);
    }

    public void addInventoryListener(@NonNull final OnInventoryListener inventoryListener) {
        OPFChecks.checkThread(true);
        inventoryListeners.add(inventoryListener);
    }

    public void addSkuInfoListener(@NonNull final OnSkuDetailsListener skuInfoListener) {
        OPFChecks.checkThread(true);
        skuDetailsListeners.add(skuInfoListener);
    }

    public void addConsumeListener(@NonNull final OnConsumeListener consumeListener) {
        OPFChecks.checkThread(true);
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
        OPFIab.register(this);
    }

    public void unsubscribe() {
        OPFIab.unregister(this);
        flush();
    }
}
