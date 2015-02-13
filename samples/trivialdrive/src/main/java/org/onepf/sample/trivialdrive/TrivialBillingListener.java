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

package org.onepf.sample.trivialdrive;

import android.support.annotation.NonNull;

import org.onepf.opfiab.OPFIab;
import org.onepf.opfiab.listener.BillingListener;
import org.onepf.opfiab.model.billing.Purchase;
import org.onepf.opfiab.model.event.SetupResponse;
import org.onepf.opfiab.model.event.billing.ConsumeResponse;
import org.onepf.opfiab.model.event.billing.InventoryResponse;
import org.onepf.opfiab.model.event.billing.PurchaseResponse;
import org.onepf.opfiab.model.event.billing.Request;
import org.onepf.opfiab.model.event.billing.Response;
import org.onepf.opfiab.model.event.billing.SkuDetailsResponse;

import java.util.Collection;


public class TrivialBillingListener implements BillingListener {

    private static final String TAG = TrivialBillingListener.class.getSimpleName();

    @Override
    public void onRequest(@NonNull final Request request) {
    }

    @Override
    public void onResponse(@NonNull final Response response) {
    }

    @Override
    public void onConsume(@NonNull final ConsumeResponse consumeResponse) {
    }

    @Override
    public void onInventory(@NonNull final InventoryResponse inventoryResponse) {
        final Collection<Purchase> inventory;
        if (inventoryResponse.isSuccessful()
                && (inventory = inventoryResponse.getInventory()) != null) {
            for (final Purchase purchase : inventory) {
//                OPFIab.getHelper().consume(purchase);
            }
        }
    }

    @Override
    public void onPurchase(@NonNull final PurchaseResponse purchaseResponse) {
        if (purchaseResponse.isSuccessful()) {
            OPFIab.getHelper().inventory();
        }
    }

    @Override
    public void onSetup(@NonNull final SetupResponse setupResponse) {
    }

    @Override
    public void onSkuDetails(@NonNull final SkuDetailsResponse skuDetailsResponse) {
    }
}
