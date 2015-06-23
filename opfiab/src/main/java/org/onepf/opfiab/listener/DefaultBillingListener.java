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
import org.onepf.opfiab.model.Configuration.Builder;
import org.onepf.opfiab.model.billing.Purchase;
import org.onepf.opfiab.model.billing.SkuType;
import org.onepf.opfiab.model.event.billing.InventoryResponse;
import org.onepf.opfiab.model.event.billing.PurchaseResponse;
import org.onepf.opfiab.verification.VerificationResult;

import java.util.Map;

/**
 * Default implementation of {@link BillingListener} interface.
 * <p>
 * Intended to be used in {@link Builder#setBillingListener(BillingListener)}.
 * <p>
 * Implements following features:
 * <ul>
 *  <li>Attempts to consume all verified consumable purchases.
 *  <li>Attempts to fully load user's inventory via subsequent calls to {@link IabHelper#inventory(boolean)}.
 * </ul>
 */
public class DefaultBillingListener extends SimpleBillingListener {

    /**
     * Lazy initialized {@link IabHelper} instance used for any billing actions performed by
     * {@link DefaultBillingListener}.
     */
    @Nullable
    private IabHelper iabHelper;

    @NonNull
    protected IabHelper getHelper() {
        if (iabHelper == null) {
            iabHelper = OPFIab.getAdvancedHelper();
        }
        return iabHelper;
    }

    /**
     * Checks if supplied purchase can be consumed.
     *
     * @param purchase Purchase object to check.
     * @return True if purchase can be consumed, false otherwise.
     * @see IabHelper#consume(Purchase)
     */
    protected boolean canConsume(@Nullable final Purchase purchase) {
        return purchase != null && purchase.getType() == SkuType.CONSUMABLE;
    }

    @Override
    public void onPurchase(@NonNull final PurchaseResponse purchaseResponse) {
        super.onPurchase(purchaseResponse);
        final Purchase purchase;
        if (purchaseResponse.isSuccessful()
                && canConsume(purchase = purchaseResponse.getPurchase())) {
            //noinspection ConstantConditions
            getHelper().consume(purchase);
        }
    }

    @SuppressWarnings("PMD.AvoidDeeplyNestedIfStmts")
    @Override
    public void onInventory(@NonNull final InventoryResponse inventoryResponse) {
        super.onInventory(inventoryResponse);
        if (inventoryResponse.isSuccessful()) {
            // Inventory request was successful
            final Map<Purchase, VerificationResult> inventory = inventoryResponse.getInventory();
            for (final Map.Entry<Purchase, VerificationResult> entry : inventory.entrySet()) {
                final VerificationResult verificationResult = entry.getValue();
                final Purchase purchase;
                if (verificationResult == VerificationResult.SUCCESS
                        && canConsume(purchase = entry.getKey())) {
                    getHelper().consume(purchase);
                }
            }
            // Load next batch if there's more
            if (inventoryResponse.hasMore()) {
                getHelper().inventory(false);
            }
        }
    }
}
