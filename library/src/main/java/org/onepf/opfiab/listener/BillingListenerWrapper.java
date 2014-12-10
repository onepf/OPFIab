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

package org.onepf.opfiab.listener;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.onepf.opfiab.BillingProvider;
import org.onepf.opfiab.model.billing.SetupStatus;
import org.onepf.opfiab.model.response.ConsumeResponse;
import org.onepf.opfiab.model.response.InventoryResponse;
import org.onepf.opfiab.model.response.PurchaseResponse;
import org.onepf.opfiab.model.response.SkuDetailsResponse;

public class BillingListenerWrapper implements BillingListener {

    @Nullable
    private final BillingListener billingListener;

    public BillingListenerWrapper(@Nullable final BillingListener billingListener) {
        this.billingListener = billingListener;
    }

    @Override
    public void onSetup(@NonNull final SetupStatus status,
                        @Nullable final BillingProvider billingProvider) {
        if (billingListener != null) {
            billingListener.onSetup(status, billingProvider);
        }
    }

    @Override
    public void onPurchase(@NonNull final PurchaseResponse purchaseResponse) {
        if (billingListener != null) {
            billingListener.onPurchase(purchaseResponse);
        }
    }

    @Override
    public void onConsume(@NonNull final ConsumeResponse consumeResponse) {
        if (billingListener != null) {
            billingListener.onConsume(consumeResponse);
        }
    }

    @Override
    public void onInventory(@NonNull final InventoryResponse inventoryResponse) {
        if (billingListener != null) {
            billingListener.onInventory(inventoryResponse);
        }
    }

    @Override
    public void onSkuInfo(@NonNull final SkuDetailsResponse skuDetailsResponse) {
        if (billingListener != null) {
            billingListener.onSkuInfo(skuDetailsResponse);
        }
    }
}
