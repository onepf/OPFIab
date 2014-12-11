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

import org.onepf.opfiab.model.event.SetupEvent;
import org.onepf.opfiab.model.event.response.ConsumeResponse;
import org.onepf.opfiab.model.event.response.InventoryResponse;
import org.onepf.opfiab.model.event.response.PurchaseResponse;
import org.onepf.opfiab.model.event.response.SkuDetailsResponse;

public class SimpleBillingListener implements BillingListener {

    @Override
    public void onSetup(@NonNull final SetupEvent setupEvent) { }

    @Override
    public void onConsume(@NonNull final ConsumeResponse consumeResponse) { }

    @Override
    public void onPurchase(@NonNull final PurchaseResponse purchaseResponse) { }

    @Override
    public void onInventory(@NonNull final InventoryResponse inventoryResponse) { }

    @Override
    public void onSkuInfo(@NonNull final SkuDetailsResponse skuDetailsResponse) { }
}
