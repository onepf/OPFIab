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
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import org.onepf.opfiab.android.OPFIabSupportFragment;
import org.onepf.opfiab.listener.BillingListener;
import org.onepf.opfiab.listener.OnConsumeListener;
import org.onepf.opfiab.listener.OnInventoryListener;
import org.onepf.opfiab.listener.OnPurchaseListener;
import org.onepf.opfiab.listener.OnSetupListener;
import org.onepf.opfiab.listener.OnSkuDetailsListener;
import org.onepf.opfiab.model.event.SupportFragmentLifecycleEvent;

import static org.onepf.opfiab.OPFIab.FRAGMENT_TAG;

public class SupportFragmentIabHelper extends IabHelperWrapper {

    @NonNull
    private final Object eventHandler = new Object() {

        public void onEvent(@NonNull final SupportFragmentLifecycleEvent event) {
            if (opfFragment != event.getFragment()) {
                return;
            }
            switch (event.getType()) {
                case ATTACH:
                    managedIabHelper.subscribe();
                    break;
                case DETACH:
                    managedIabHelper.unsubscribe();
                    break;
            }
        }
    };

    @NonNull
    private final Fragment fragment;

    @NonNull
    private final ManagedIabHelper managedIabHelper;

    @NonNull
    private final Fragment opfFragment;

    public SupportFragmentIabHelper(@NonNull final ManagedIabHelper managedIabHelper,
                                    @NonNull final Fragment fragment) {
        super(managedIabHelper);
        this.managedIabHelper = managedIabHelper;
        this.fragment = fragment;
        eventBus.register(eventHandler);

        Fragment opfFragment;
        final FragmentManager fragmentManager = fragment.getChildFragmentManager();
        if ((opfFragment = fragmentManager.findFragmentByTag(FRAGMENT_TAG)) == null) {
            opfFragment = OPFIabSupportFragment.newInstance();
            fragmentManager.executePendingTransactions();
            fragmentManager.beginTransaction()
                    .add(opfFragment, FRAGMENT_TAG)
                    .commit();
        }
        this.opfFragment = opfFragment;
    }

    @NonNull
    public Fragment getFragment() {
        return fragment;
    }

    @Override
    public void onActivityResult(@NonNull final Activity activity, final int requestCode,
                                 final int resultCode,
                                 @Nullable final Intent data) {
        throw new UnsupportedOperationException();
    }

    public void addBillingListener(@NonNull final BillingListener billingListener) {
        managedIabHelper.addBillingListener(billingListener);
    }

    public void addConsumeListener(@NonNull final OnConsumeListener consumeListener) {
        managedIabHelper.addConsumeListener(consumeListener);
    }

    public void addSkuInfoListener(@NonNull final OnSkuDetailsListener skuInfoListener) {
        managedIabHelper.addSkuInfoListener(skuInfoListener);
    }

    public void addInventoryListener(@NonNull final OnInventoryListener inventoryListener) {
        managedIabHelper.addInventoryListener(inventoryListener);
    }

    public void addPurchaseListener(@NonNull final OnPurchaseListener purchaseListener) {
        managedIabHelper.addPurchaseListener(purchaseListener);
    }

    public void addSetupListener(@NonNull final OnSetupListener setupListener) {
        managedIabHelper.addSetupListener(setupListener);
    }

    public void dispose() {
        eventBus.unregister(eventHandler);
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        dispose();
    }
}
