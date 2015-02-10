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

import org.onepf.opfiab.model.event.SetupResponse;
import org.onepf.opfiab.model.event.billing.Request;
import org.onepf.opfiab.model.event.billing.ConsumeResponse;
import org.onepf.opfiab.model.event.billing.InventoryResponse;
import org.onepf.opfiab.model.event.billing.PurchaseResponse;
import org.onepf.opfiab.model.event.billing.Response;
import org.onepf.opfiab.model.event.billing.SkuDetailsResponse;

public class BillingListenerWrapper implements BillingListener {

    @Nullable
    private final BillingListener billingListener;

    public BillingListenerWrapper(@Nullable final BillingListener billingListener) {
        this.billingListener = billingListener;
    }

    @Override
    public void onSetup(@NonNull final SetupResponse setupResponse) {
        if (billingListener != null) {
            billingListener.onSetup(setupResponse);
        }
    }

    @Override
    public void onRequest(@NonNull final Request request) {
        if (billingListener != null) {
            billingListener.onRequest(request);
        }
    }

    @Override
    public void onResponse(@NonNull final Response response) {
        if (billingListener != null) {
            billingListener.onResponse(response);
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
    public void onSkuDetails(@NonNull final SkuDetailsResponse skuDetailsResponse) {
        if (billingListener != null) {
            billingListener.onSkuDetails(skuDetailsResponse);
        }
    }
}
