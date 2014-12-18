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
import org.onepf.opfiab.listener.OnSkuDetailsListener;
import org.onepf.opfiab.model.billing.ConsumableDetails;
import org.onepf.opfiab.model.billing.EntitlementDetails;
import org.onepf.opfiab.model.billing.SubscriptionDetails;

public abstract class SelfManagedIabHelper extends IabHelperWrapper {

    @NonNull
    protected final ManagedIabHelper managedIabHelper;

    protected SelfManagedIabHelper(@NonNull final ManagedIabHelper managedIabHelper) {
        super(managedIabHelper);
        this.managedIabHelper = managedIabHelper;
    }

    @Override
    public void onActivityResult(@NonNull final Activity activity, final int requestCode,
                                 final int resultCode,
                                 @Nullable final Intent data) {
        throw new UnsupportedOperationException();
    }

    public abstract void purchase(@NonNull final ConsumableDetails consumableDetails);

    public abstract void purchase(@NonNull final SubscriptionDetails subscriptionDetails);

    public abstract void purchase(@NonNull final EntitlementDetails entitlementDetails);

    public void addSetupListener(
            @NonNull final OnSetupListener setupListener) {
        managedIabHelper.addSetupListener(setupListener);
    }

    public void addPurchaseListener(
            @NonNull final OnPurchaseListener purchaseListener) {
        managedIabHelper.addPurchaseListener(purchaseListener);
    }

    public void addInventoryListener(
            @NonNull final OnInventoryListener inventoryListener) {
        managedIabHelper.addInventoryListener(inventoryListener);
    }

    public void addSkuInfoListener(
            @NonNull final OnSkuDetailsListener skuInfoListener) {
        managedIabHelper.addSkuInfoListener(skuInfoListener);
    }

    public void addConsumeListener(
            @NonNull final OnConsumeListener consumeListener) {
        managedIabHelper.addConsumeListener(consumeListener);
    }

    public void addBillingListener(
            @NonNull final BillingListener billingListener) {
        managedIabHelper.addBillingListener(billingListener);
    }

    protected void dispose() { }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        dispose();
    }
}
