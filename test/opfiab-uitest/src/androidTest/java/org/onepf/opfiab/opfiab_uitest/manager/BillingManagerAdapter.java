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

package org.onepf.opfiab.opfiab_uitest.manager;

import android.support.annotation.NonNull;

import org.onepf.opfiab.listener.BillingListener;
import org.onepf.opfiab.model.event.SetupResponse;
import org.onepf.opfiab.model.event.SetupStartedEvent;
import org.onepf.opfiab.model.event.billing.BillingRequest;
import org.onepf.opfiab.model.event.billing.BillingResponse;
import org.onepf.opfiab.model.event.billing.ConsumeResponse;
import org.onepf.opfiab.model.event.billing.InventoryResponse;
import org.onepf.opfiab.model.event.billing.PurchaseResponse;
import org.onepf.opfiab.model.event.billing.SkuDetailsResponse;

/**
 * @author antonpp
 * @since 29.05.15
 */
public class BillingManagerAdapter extends TestManagerAdapter implements BillingListener {

    public BillingManagerAdapter(final TestManager testManager) {
        super(testManager);
    }

    public BillingManagerAdapter() {
        super();
    }

    @Override
    public void onRequest(@NonNull BillingRequest billingRequest) {
        validateEvent(billingRequest);
    }

    @Override
    public void onResponse(@NonNull BillingResponse billingResponse) {
        // already handled
    }

    @Override
    public void onConsume(@NonNull ConsumeResponse consumeResponse) {
        validateEvent(consumeResponse);
    }

    @Override
    public void onInventory(@NonNull InventoryResponse inventoryResponse) {
        validateEvent(inventoryResponse);
    }

    @Override
    public void onPurchase(@NonNull PurchaseResponse purchaseResponse) {
        validateEvent(purchaseResponse);
    }

    @Override
    public void onSetupStarted(@NonNull SetupStartedEvent setupStartedEvent) {
        validateEvent(setupStartedEvent);
    }

    @Override
    public void onSetupResponse(@NonNull SetupResponse setupResponse) {
        validateEvent(setupResponse);
    }

    @Override
    public void onSkuDetails(@NonNull SkuDetailsResponse skuDetailsResponse) {
        validateEvent(skuDetailsResponse);
    }
}
