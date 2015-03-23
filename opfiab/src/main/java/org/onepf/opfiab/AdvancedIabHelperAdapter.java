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

package org.onepf.opfiab;

import android.support.annotation.NonNull;

import org.onepf.opfiab.listener.BillingListener;
import org.onepf.opfiab.listener.OnConsumeListener;
import org.onepf.opfiab.listener.OnInventoryListener;
import org.onepf.opfiab.listener.OnPurchaseListener;
import org.onepf.opfiab.listener.OnSetupListener;
import org.onepf.opfiab.listener.OnSkuDetailsListener;
import org.onepf.opfiab.model.event.billing.BillingRequest;

public class AdvancedIabHelperAdapter extends IabHelper {

    protected final AdvancedIabHelper advancedIabHelper;

    public AdvancedIabHelperAdapter(final AdvancedIabHelper advancedIabHelper) {
        super();
        this.advancedIabHelper = advancedIabHelper;
    }

    @Override
    public void postRequest(@NonNull final BillingRequest billingRequest) {
        advancedIabHelper.postRequest(billingRequest);
    }

    public void addSetupListener(
            @NonNull final OnSetupListener setupListener) {
        advancedIabHelper.addSetupListener(setupListener);
    }

    public void addPurchaseListener(
            @NonNull final OnPurchaseListener purchaseListener) {
        advancedIabHelper.addPurchaseListener(purchaseListener);
    }

    public void addInventoryListener(
            @NonNull final OnInventoryListener inventoryListener) {
        advancedIabHelper.addInventoryListener(inventoryListener);
    }

    public void addSkuDetailsListener(
            @NonNull final OnSkuDetailsListener skuInfoListener) {
        advancedIabHelper.addSkuDetailsListener(skuInfoListener);
    }

    public void addConsumeListener(
            @NonNull final OnConsumeListener consumeListener) {
        advancedIabHelper.addConsumeListener(consumeListener);
    }

    public void addBillingListener(
            @NonNull final BillingListener billingListener) {
        advancedIabHelper.addBillingListener(billingListener);
    }
}
