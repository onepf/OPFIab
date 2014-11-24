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

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.onepf.opfiab.listener.BillingListener;
import org.onepf.opfiab.listener.OnConsumeListener;
import org.onepf.opfiab.listener.OnInventoryListener;
import org.onepf.opfiab.listener.OnPurchaseListener;
import org.onepf.opfiab.listener.OnSetupListener;
import org.onepf.opfiab.listener.OnSkuInfoListener;
import org.onepf.opfiab.listener.OnSubscriptionListener;
import org.onepf.opfiab.model.Consumable;
import org.onepf.opfiab.model.Subscription;
import org.onepf.opfiab.model.response.ConsumeResponse;
import org.onepf.opfiab.model.response.InventoryResponse;
import org.onepf.opfiab.model.response.PurchaseResponse;
import org.onepf.opfiab.model.response.SkuInfoResponse;
import org.onepf.opfiab.model.response.SubscriptionResponse;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class ManagedOPFIabHelper extends OPFIabHelperWrapper implements ManagedLifecycle {

    @NonNull
    final Set<OnSetupListener> setupListeners = Collections.synchronizedSet(
            new HashSet<OnSetupListener>());

    @NonNull
    final Set<OnPurchaseListener> purchaseListeners = Collections.synchronizedSet(
            new HashSet<OnPurchaseListener>());

    @NonNull
    final Set<OnSubscriptionListener> subscriptionListeners = Collections.synchronizedSet(
            new HashSet<OnSubscriptionListener>());

    @NonNull
    final Set<OnInventoryListener> inventoryListeners = Collections.synchronizedSet(
            new HashSet<OnInventoryListener>());

    @NonNull
    final Set<OnSkuInfoListener> skuInfoListeners = Collections.synchronizedSet(
            new HashSet<OnSkuInfoListener>());

    @NonNull
    final Set<OnConsumeListener> consumeListeners = Collections.synchronizedSet(
            new HashSet<OnConsumeListener>());

    @NonNull
    private final BillingListenerCompositor listenerCompositor = OPFIab.instance.billingListenerCompositor;

    public ManagedOPFIabHelper(final OPFIabHelper opfIabHelper) {
        super(opfIabHelper);
    }

    public void addSetupListener(@NonNull final OnSetupListener setupListener) {
        setupListeners.add(setupListener);
    }

    public void addPurchaseListener(@NonNull final OnPurchaseListener purchaseListener) {
        purchaseListeners.add(purchaseListener);
    }

    public void addSubscriptionListener(
            @NonNull final OnSubscriptionListener subscriptionListener) {
        subscriptionListeners.add(subscriptionListener);
    }

    public void addInventoryListener(@NonNull final OnInventoryListener inventoryListener) {
        inventoryListeners.add(inventoryListener);
    }

    public void addSkuInfoListener(@NonNull final OnSkuInfoListener skuInfoListener) {
        skuInfoListeners.add(skuInfoListener);
    }

    public void addConsumeListener(@NonNull final OnConsumeListener consumeListener) {
        consumeListeners.add(consumeListener);
    }

    public void addBillingListener(@NonNull final BillingListener billingListener) {
        addSetupListener(billingListener);
        addPurchaseListener(billingListener);
        addSubscriptionListener(billingListener);
        addInventoryListener(billingListener);
        addSkuInfoListener(billingListener);
        addConsumeListener(billingListener);
    }

    public void purchase(@NonNull final Activity activity,
                         @NonNull final Consumable consumable,
                         @NonNull final OnPurchaseListener purchaseListener) {
        addPurchaseListener(new OnPurchaseListener() {
            @Override
            public void onPurchase(@NonNull final PurchaseResponse purchaseResponse) {
                purchaseListener.onPurchase(purchaseResponse);
                purchaseListeners.remove(this);
            }
        });
        super.purchase(activity, consumable);
    }

    public void consume(@Nullable final Activity activity,
                        @NonNull final Consumable consumable,
                        @NonNull final OnConsumeListener consumeListener) {
        addConsumeListener(new OnConsumeListener() {
            @Override
            public void onConsume(@NonNull final ConsumeResponse consumeResponse) {
                consumeListener.onConsume(consumeResponse);
                consumeListeners.remove(this);
            }
        });
        super.consume(activity, consumable);
    }

    public void subscribe(@Nullable final Activity activity,
                          @NonNull final Subscription subscription,
                          @NonNull final OnSubscriptionListener subscriptionListener) {
        addSubscriptionListener(new OnSubscriptionListener() {
            @Override
            public void onSubscription(@NonNull final SubscriptionResponse subscriptionResponse) {
                subscriptionListener.onSubscription(subscriptionResponse);
                subscriptionListeners.remove(this);
            }
        });
        super.subscribe(activity, subscription);
    }

    public void inventory(@Nullable final Activity activity,
                          @NonNull final OnInventoryListener inventoryListener) {
        addInventoryListener(new OnInventoryListener() {
            @Override
            public void onInventory(@NonNull final InventoryResponse inventoryResponse) {
                inventoryListener.onInventory(inventoryResponse);
                inventoryListeners.remove(this);
            }
        });
        super.inventory(activity);
    }

    public void skuInfo(@Nullable final Activity activity,
                        @NonNull final String sku,
                        @NonNull final OnSkuInfoListener skuInfoListener) {
        addSkuInfoListener(new OnSkuInfoListener() {
            @Override
            public void onSkuInfo(@NonNull final SkuInfoResponse skuInfoResponse) {
                skuInfoListener.onSkuInfo(skuInfoResponse);
                skuInfoListeners.remove(this);
            }
        });
        super.skuInfo(activity, sku);
    }

    public void onCreate() {
    }

    public void onResume() {
        listenerCompositor.register(this);
    }

    public void onPause() {
        listenerCompositor.unregister(this);
    }

    @Override
    public boolean onActivityResult(final int requestCode, final int resultCode,
                                    @Nullable final Intent data) {
        return false;
    }
}
