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

import org.onepf.opfiab.billing.BillingProvider;
import org.onepf.opfiab.billing.SetupStatus;
import org.onepf.opfiab.listener.BillingListener;
import org.onepf.opfiab.listener.BillingListenerWrapper;
import org.onepf.opfiab.listener.OnConsumeListener;
import org.onepf.opfiab.listener.OnInventoryListener;
import org.onepf.opfiab.listener.OnPurchaseListener;
import org.onepf.opfiab.listener.OnSetupListener;
import org.onepf.opfiab.listener.OnSkuInfoListener;
import org.onepf.opfiab.listener.OnSubscriptionListener;
import org.onepf.opfiab.model.response.ConsumeResponse;
import org.onepf.opfiab.model.response.InventoryResponse;
import org.onepf.opfiab.model.response.PurchaseResponse;
import org.onepf.opfiab.model.response.SkuInfoResponse;
import org.onepf.opfiab.model.response.SubscriptionResponse;
import org.onepf.opfiab.util.OPFUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

class BillingListenerCompositor extends BillingListenerWrapper {

    @NonNull
    private Set<ManagedOPFIabHelper> helpers = new HashSet<>();

    BillingListenerCompositor(@Nullable final BillingListener billingListener) {
        super(billingListener);
    }

    void register(@NonNull final ManagedOPFIabHelper managedOPFIabHelper) {
        OPFUtils.checkThread(true);
        helpers.add(managedOPFIabHelper);
    }

    void unregister(@NonNull final ManagedOPFIabHelper managedOPFIabHelper) {
        OPFUtils.checkThread(true);
        helpers.remove(managedOPFIabHelper);
    }

    @Override
    public void onSetup(@NonNull final SetupStatus status,
                        @Nullable final BillingProvider billingProvider) {
        super.onSetup(status, billingProvider);
        for (final ManagedOPFIabHelper helper : helpers) {
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
        for (final ManagedOPFIabHelper helper : helpers) {
            final Collection<OnPurchaseListener> purchaseListeners =
                    Collections.unmodifiableCollection(helper.purchaseListeners);
            for (final OnPurchaseListener purchaseListener : purchaseListeners) {
                purchaseListener.onPurchase(purchaseResponse);
            }
        }
    }

    @Override
    public void onSubscription(@NonNull final SubscriptionResponse subscriptionResponse) {
        super.onSubscription(subscriptionResponse);
        for (final ManagedOPFIabHelper helper : helpers) {
            final Collection<OnSubscriptionListener> subscriptionListeners =
                    Collections.unmodifiableCollection(helper.subscriptionListeners);
            for (final OnSubscriptionListener subscriptionListener : subscriptionListeners) {
                subscriptionListener.onSubscription(subscriptionResponse);
            }
        }
    }

    @Override
    public void onInventory(@NonNull final InventoryResponse inventoryResponse) {
        super.onInventory(inventoryResponse);
        for (final ManagedOPFIabHelper helper : helpers) {
            final Collection<OnInventoryListener> inventoryListeners =
                    Collections.unmodifiableCollection(helper.inventoryListeners);
            for (final OnInventoryListener inventoryListener : inventoryListeners) {
                inventoryListener.onInventory(inventoryResponse);
            }
        }
    }

    @Override
    public void onSkuInfo(@NonNull final SkuInfoResponse skuInfoResponse) {
        super.onSkuInfo(skuInfoResponse);
        for (final ManagedOPFIabHelper helper : helpers) {
            final Collection<OnSkuInfoListener> skuInfoListeners =
                    Collections.unmodifiableCollection(helper.skuInfoListeners);
            for (final OnSkuInfoListener skuInfoListener : skuInfoListeners) {
                skuInfoListener.onSkuInfo(skuInfoResponse);
            }
        }
    }

    @Override
    public void onConsume(@NonNull final ConsumeResponse consumeResponse) {
        super.onConsume(consumeResponse);
        for (final ManagedOPFIabHelper helper : helpers) {
            final Collection<OnConsumeListener> consumeListeners =
                    Collections.unmodifiableCollection(helper.consumeListeners);
            for (final OnConsumeListener consumeListener : consumeListeners) {
                consumeListener.onConsume(consumeResponse);
            }
        }
    }
}
