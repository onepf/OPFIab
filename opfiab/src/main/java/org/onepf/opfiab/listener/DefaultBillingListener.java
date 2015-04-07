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

package org.onepf.opfiab.listener;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.onepf.opfiab.OPFIab;
import org.onepf.opfiab.api.IabHelper;
import org.onepf.opfiab.model.billing.Purchase;
import org.onepf.opfiab.model.billing.SkuType;
import org.onepf.opfiab.model.event.billing.InventoryResponse;
import org.onepf.opfiab.model.event.billing.PurchaseResponse;
import org.onepf.opfiab.verification.VerificationResult;

import java.util.Map;

public class DefaultBillingListener extends SimpleBillingListener {

    @Nullable
    private IabHelper iabHelper;

    @NonNull
    protected IabHelper getHelper() {
        if (iabHelper == null) {
            iabHelper = OPFIab.getAdvancedHelper();
        }
        return iabHelper;
    }

    @Override
    public void onPurchase(@NonNull final PurchaseResponse purchaseResponse) {
        super.onPurchase(purchaseResponse);
        if (purchaseResponse.isSuccessful()) {
            getHelper().inventory(true);
        }
    }

    @SuppressWarnings("PMD.AvoidDeeplyNestedIfStmts")
    @Override
    public void onInventory(@NonNull final InventoryResponse inventoryResponse) {
        super.onInventory(inventoryResponse);
        if (inventoryResponse.isSuccessful()) {
            final Map<Purchase, VerificationResult> inventory = inventoryResponse.getInventory();
            if (inventory != null) {
                for (final Map.Entry<Purchase, VerificationResult> entry : inventory.entrySet()) {
                    final Purchase purchase = entry.getKey();
                    if (purchase.getType() == SkuType.CONSUMABLE
                            && entry.getValue() == VerificationResult.SUCCESS) {
                        consume(purchase);
                    }
                }
            }
            if (inventoryResponse.hasMore()) {
                getHelper().inventory(false);
            }
        }
    }

    protected void consume(final Purchase purchase) {
        getHelper().consume(purchase);
    }
}
