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
import org.onepf.opfiab.model.billing.SetupStatus;
import org.onepf.opfiab.model.response.ConsumeResponse;
import org.onepf.opfiab.model.response.InventoryResponse;
import org.onepf.opfiab.model.response.PurchaseResponse;
import org.onepf.opfiab.model.response.SkuDetailsResponse;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

class GlobalBillingListener extends BillingListenerWrapper {

    @NonNull
    private Set<ManagedIabHelper> helpers = new HashSet<>();

    GlobalBillingListener(@Nullable final BillingListener billingListener) {
        super(billingListener);
    }

    void subscribe(@NonNull final ManagedIabHelper managedOPFIabHelper) {
        OPFUtils.checkThread(true);
        helpers.add(managedOPFIabHelper);
    }

    void unsubscribe(@NonNull final ManagedIabHelper managedOPFIabHelper) {
        OPFUtils.checkThread(true);
        helpers.remove(managedOPFIabHelper);
    }

    @Override
    public void onSetup(@NonNull final SetupStatus status,
                        @Nullable final BillingProvider billingProvider) {
        super.onSetup(status, billingProvider);
        for (final ManagedIabHelper helper : helpers) {
            final Collection<OnSetupListener> setupListeners =
                    Collections.unmodifiableCollection(helper.setupListeners);
            for (final OnSetupListener setupListener : setupListeners) {
                setupListener.onSetup(status, billingProvider);
            }
        }
    }

    @Override
    public void onPurchase(@NonNull final PurchaseResponse purchaseResponse) {
        super.onPurchase(purchaseResponse);
        for (final ManagedIabHelper helper : helpers) {
            final Collection<OnPurchaseListener> purchaseListeners =
                    Collections.unmodifiableCollection(helper.purchaseListeners);
            for (final OnPurchaseListener purchaseListener : purchaseListeners) {
                purchaseListener.onPurchase(purchaseResponse);
            }
        }
    }

    @Override
    public void onInventory(@NonNull final InventoryResponse inventoryResponse) {
        super.onInventory(inventoryResponse);
        for (final ManagedIabHelper helper : helpers) {
            final Collection<OnInventoryListener> inventoryListeners =
                    Collections.unmodifiableCollection(helper.inventoryListeners);
            for (final OnInventoryListener inventoryListener : inventoryListeners) {
                inventoryListener.onInventory(inventoryResponse);
            }
        }
    }

    @Override
    public void onSkuInfo(@NonNull final SkuDetailsResponse skuDetailsResponse) {
        super.onSkuInfo(skuDetailsResponse);
        for (final ManagedIabHelper helper : helpers) {
            final Collection<OnSkuDetailsListener> skuInfoListeners =
                    Collections.unmodifiableCollection(helper.skuInfoListeners);
            for (final OnSkuDetailsListener skuInfoListener : skuInfoListeners) {
                skuInfoListener.onSkuInfo(skuDetailsResponse);
            }
        }
    }

    @Override
    public void onConsume(@NonNull final ConsumeResponse consumeResponse) {
        super.onConsume(consumeResponse);
        for (final ManagedIabHelper helper : helpers) {
            final Collection<OnConsumeListener> consumeListeners =
                    Collections.unmodifiableCollection(helper.consumeListeners);
            for (final OnConsumeListener consumeListener : consumeListeners) {
                consumeListener.onConsume(consumeResponse);
            }
        }
    }
}
