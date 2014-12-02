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

import android.app.Fragment;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.onepf.opfiab.listener.BillingListener;
import org.onepf.opfiab.listener.OnConsumeListener;
import org.onepf.opfiab.listener.OnInventoryListener;
import org.onepf.opfiab.listener.OnPurchaseListener;
import org.onepf.opfiab.listener.OnSetupListener;
import org.onepf.opfiab.listener.OnSkuInfoListener;

public class FragmentIabHelper extends IabHelperWrapper {

    @NonNull
    private final Fragment fragment;

    @NonNull
    private final ManagedIabHelper managedIabHelper;

    public FragmentIabHelper(@NonNull final ManagedIabHelper managedIabHelper,
                             @NonNull final Fragment fragment) {
        super(managedIabHelper);
        this.managedIabHelper = managedIabHelper;
        this.fragment = fragment;
        // TODO: add OPFFragment
    }

    @NonNull
    public Fragment getFragment() {
        return fragment;
    }

    @Override
    public void onActivityResult(final int requestCode, final int resultCode,
                                 @Nullable final Intent data) {
        throw new UnsupportedOperationException();
    }

    public void addBillingListener(
            @NonNull final BillingListener billingListener) {
        managedIabHelper.addBillingListener(billingListener);
    }

    public void addConsumeListener(
            @NonNull final OnConsumeListener consumeListener) {
        managedIabHelper.addConsumeListener(consumeListener);
    }

    public void addSkuInfoListener(
            @NonNull final OnSkuInfoListener skuInfoListener) {
        managedIabHelper.addSkuInfoListener(skuInfoListener);
    }

    public void addInventoryListener(
            @NonNull final OnInventoryListener inventoryListener) {
        managedIabHelper.addInventoryListener(inventoryListener);
    }

    public void addPurchaseListener(
            @NonNull final OnPurchaseListener purchaseListener) {
        managedIabHelper.addPurchaseListener(purchaseListener);
    }

    public void addSetupListener(
            @NonNull final OnSetupListener setupListener) {
        managedIabHelper.addSetupListener(setupListener);
    }
}
