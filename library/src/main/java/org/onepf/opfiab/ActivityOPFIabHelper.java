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
import android.support.annotation.NonNull;

import org.onepf.opfiab.listener.BillingListener;
import org.onepf.opfiab.listener.OnConsumeListener;
import org.onepf.opfiab.listener.OnInventoryListener;
import org.onepf.opfiab.listener.OnPurchaseListener;
import org.onepf.opfiab.listener.OnSetupListener;
import org.onepf.opfiab.listener.OnSkuInfoListener;
import org.onepf.opfiab.listener.OnSubscriptionListener;
import org.onepf.opfiab.model.Consumable;
import org.onepf.opfiab.model.Subscription;

public class ActivityOPFIabHelper extends OPFIabHelperWrapper implements StandaloneOPFIabHelper {

    @NonNull
    private final Activity activity;

    @NonNull
    private final ManagedOPFIabHelper opfIabHelper;

    public ActivityOPFIabHelper(@NonNull final ManagedOPFIabHelper opfIabHelper,
                                @NonNull final Activity activity) {
        super(opfIabHelper);
        this.opfIabHelper = opfIabHelper;
        this.activity = activity;
    }

    @NonNull
    public Activity getActivity() {
        return activity;
    }

    public void addSetupListener(@NonNull final OnSetupListener setupListener) {
        opfIabHelper.addSetupListener(setupListener);
    }

    public void addSkuInfoListener(@NonNull final OnSkuInfoListener skuInfoListener) {
        opfIabHelper.addSkuInfoListener(skuInfoListener);
    }

    public void addInventoryListener(@NonNull final OnInventoryListener inventoryListener) {
        opfIabHelper.addInventoryListener(inventoryListener);
    }

    public void addConsumeListener(@NonNull final OnConsumeListener consumeListener) {
        opfIabHelper.addConsumeListener(consumeListener);
    }

    public void addPurchaseListener(@NonNull final OnPurchaseListener purchaseListener) {
        opfIabHelper.addPurchaseListener(purchaseListener);
    }

    public void addSubscriptionListener(
            @NonNull final OnSubscriptionListener subscriptionListener) {
        opfIabHelper.addSubscriptionListener(subscriptionListener);
    }

    public void addBillingListener(@NonNull final BillingListener billingListener) {
        opfIabHelper.addBillingListener(billingListener);
    }

    public void purchase(@NonNull final Consumable consumable,
                         @NonNull final OnPurchaseListener purchaseListener) {
        opfIabHelper.purchase(activity, consumable, purchaseListener);
    }

    public void inventory(@NonNull final OnInventoryListener inventoryListener) {
        opfIabHelper.inventory(activity, inventoryListener);
    }

    public void consume(@NonNull final Consumable consumable,
                        @NonNull final OnConsumeListener consumeListener) {
        opfIabHelper.consume(activity, consumable, consumeListener);
    }

    public void skuInfo(@NonNull final String sku,
                        @NonNull final OnSkuInfoListener skuInfoListener) {
        opfIabHelper.skuInfo(activity, sku, skuInfoListener);
    }

    public void subscribe(@NonNull final Subscription subscription,
                          @NonNull final OnSubscriptionListener subscriptionListener) {
        opfIabHelper.subscribe(activity, subscription, subscriptionListener);
    }

    @Override
    public void purchase(@NonNull final Consumable consumable) {
        purchase(activity, consumable);
    }

    @Override
    public void consume(@NonNull final Consumable consumable) {
        consume(activity, consumable);
    }

    @Override
    public void subscribe(@NonNull final Subscription subscription) {
        subscribe(activity, subscription);
    }

    @Override
    public void inventory() {
        inventory(activity);
    }

    @Override
    public void skuInfo(@NonNull final String sku) {
        skuInfo(activity, sku);
    }
}


