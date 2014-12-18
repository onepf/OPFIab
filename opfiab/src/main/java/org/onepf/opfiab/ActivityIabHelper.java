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

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;

import org.onepf.opfiab.android.OPFIabFragment;
import org.onepf.opfiab.android.OPFIabSupportFragment;
import org.onepf.opfiab.listener.BillingListener;
import org.onepf.opfiab.listener.OnConsumeListener;
import org.onepf.opfiab.listener.OnInventoryListener;
import org.onepf.opfiab.listener.OnPurchaseListener;
import org.onepf.opfiab.listener.OnSetupListener;
import org.onepf.opfiab.listener.OnSkuDetailsListener;
import org.onepf.opfiab.model.event.FragmentLifecycleEvent;
import org.onepf.opfiab.model.event.SupportFragmentLifecycleEvent;

import static org.onepf.opfiab.OPFIab.FRAGMENT_TAG;

public class ActivityIabHelper extends IabHelperWrapper {

    @NonNull
    private final Object eventHandler = new Object() {

        public void onEvent(@NonNull final FragmentLifecycleEvent event) {
            if (opfFragment != event.getFragment()) {
                return;
            }
            switch (event.getType()) {
                case ATTACH:
                    managedIabHelper.subscribe();
                    break;
                case DETACH:
                    managedIabHelper.unsubscribe();
                    dispose();
                    break;
            }
        }

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
                    dispose();
                    break;
            }
        }
    };

    @NonNull
    private final Activity activity;

    @NonNull
    private final ManagedIabHelper managedIabHelper;

    @NonNull
    private final Object opfFragment;

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public ActivityIabHelper(@NonNull final ManagedIabHelper managedIabHelper,
                             @NonNull final Activity activity) {
        super(managedIabHelper);
        this.managedIabHelper = managedIabHelper;
        this.activity = activity;
        eventBus.register(eventHandler);

        android.app.Fragment fragment;
        final android.app.FragmentManager fragmentManager = activity.getFragmentManager();
        if ((fragment = fragmentManager.findFragmentByTag(FRAGMENT_TAG)) == null) {
            fragment = OPFIabFragment.newInstance();
            fragmentManager.executePendingTransactions();
            fragmentManager.beginTransaction()
                    .add(fragment, FRAGMENT_TAG)
                    .commit();
        }
        this.opfFragment = fragment;
    }

    public ActivityIabHelper(@NonNull final ManagedIabHelper managedIabHelper,
                             @NonNull final FragmentActivity activity) {
        super(managedIabHelper);
        this.managedIabHelper = managedIabHelper;
        this.activity = activity;
        eventBus.register(eventHandler);

        android.support.v4.app.Fragment fragment;
        final android.support.v4.app.FragmentManager fragmentManager =
                activity.getSupportFragmentManager();
        if ((fragment = fragmentManager.findFragmentByTag(FRAGMENT_TAG)) == null) {
            fragment = OPFIabSupportFragment.newInstance();
            fragmentManager.executePendingTransactions();
            fragmentManager.beginTransaction()
                    .add(fragment, FRAGMENT_TAG)
                    .commit();
        }
        this.opfFragment = fragment;
    }

    @NonNull
    public Activity getActivity() {
        return activity;
    }

    @Override
    public void onActivityResult(@NonNull final Activity activity, final int requestCode,
                                 final int resultCode,
                                 @Nullable final Intent data) {
        throw new UnsupportedOperationException();
    }

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

    protected void dispose() {
        eventBus.unregister(eventHandler);
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        dispose();
    }
}
