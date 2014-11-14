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

package org.onepf.opfiab.api;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.SparseArray;

import org.onepf.opfiab.billing.BillingProvider;
import org.onepf.opfiab.listener.BillingListener;
import org.onepf.opfiab.listener.BillingListenerWrapper;
import org.onepf.opfiab.listener.OnInventoryListener;
import org.onepf.opfiab.listener.OnPurchaseListener;
import org.onepf.opfiab.listener.OnSetupListener;
import org.onepf.opfiab.listener.OnSkuInfoListener;
import org.onepf.opfiab.listener.OnSubscriptionListener;
import org.onepf.opfiab.model.InventoryResponse;
import org.onepf.opfiab.model.PurchaseResponse;
import org.onepf.opfiab.model.SetupStatus;
import org.onepf.opfiab.model.SkuInfoResponse;
import org.onepf.opfiab.model.SubscriptionResponse;

import java.util.HashSet;
import java.util.Set;

class BillingListenerCompositor extends BillingListenerWrapper {

    @NonNull
    private final Set<OnSetupListener> setupListeners = new HashSet<>();
    @NonNull
    private final SparseArray<OnPurchaseListener> purchaseListeners = new SparseArray<>();
    @NonNull
    private final SparseArray<OnSubscriptionListener> subscriptionListeners = new SparseArray<>();
    @NonNull
    private final SparseArray<OnInventoryListener> inventoryListeners = new SparseArray<>();
    @NonNull
    private final SparseArray<OnSkuInfoListener> skuInfoListeners = new SparseArray<>();

    BillingListenerCompositor(final @Nullable BillingListener billingListener) {
        super(billingListener);
    }


    void register(final @NonNull ManagedOPFIabHelper managedOPFIabHelper) {

    }

    void unregister(final @NonNull ManagedOPFIabHelper managedOPFIabHelper) {

    }

    @Override
    public void onSetup(final @NonNull SetupStatus status,
                        final @Nullable BillingProvider billingProvider) {
        super.onSetup(status, billingProvider);
        for (final OnSetupListener setupListener : setupListeners) {
            setupListener.onSetup(status, billingProvider);
        }
    }

    @Override
    public void onPurchase(final @NonNull PurchaseResponse purchaseResponse) {
        super.onPurchase(purchaseResponse);
        final OnPurchaseListener purchaseListener;
        if ((purchaseListener = purchaseListeners.get(purchaseResponse.getId())) != null) {
            purchaseListener.onPurchase(purchaseResponse);
        }
    }

    @Override
    public void onSubscription(final @NonNull SubscriptionResponse subscriptionResponse) {
        super.onSubscription(subscriptionResponse);
        final OnSubscriptionListener subscriptionListener;
        if ((subscriptionListener = subscriptionListeners.get(subscriptionResponse.getId()))
                != null) {
            subscriptionListener.onSubscription(subscriptionResponse);
        }
    }

    @Override
    public void onInventory(final @NonNull InventoryResponse inventoryResponse) {
        super.onInventory(inventoryResponse);
        final OnInventoryListener inventoryListener;
        if ((inventoryListener = inventoryListeners.get(inventoryResponse.getId())) != null) {
            inventoryListener.onInventory(inventoryResponse);
        }
    }

    @Override
    public void onSkuInfo(final @NonNull SkuInfoResponse skuInfoResponse) {
        super.onSkuInfo(skuInfoResponse);
        final OnSkuInfoListener skuInfoListener;
        if ((skuInfoListener = skuInfoListeners.get(skuInfoResponse.getId())) != null) {
            skuInfoListener.onSkuInfo(skuInfoResponse);
        }
    }
}
