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
import org.onepf.opfiab.model.ComponentState;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

public abstract class SelfManagedIabHelper extends IabHelperAdapter {

    protected static final String FRAGMENT_TAG = "OPFIabFragment";


    @NonNull
    protected final ManagedIabHelper managedIabHelper;

    SelfManagedIabHelper(@NonNull final ManagedIabHelper managedIabHelper) {
        super(managedIabHelper);
        this.managedIabHelper = managedIabHelper;
        OPFIab.register(this);
    }

    @NonNull
    protected abstract Activity getActivity();

    protected void handleLifecycle(@NonNull final ComponentState type) {
        // Handle billing events depending on fragment lifecycle
        if (type == ComponentState.ATTACH || type == ComponentState.START || type == ComponentState.RESUME) {
            // Attach - subscribe for event right away when helper is created
            // Start - necessary to handle onActivityResult since it's called before onResume
            // Resume - re-subscribe for billing events if we unsubscribed in onPause
            managedIabHelper.subscribe();
        } else if (type == ComponentState.STOP || type == ComponentState.PAUSE) {
            // Pause - only callback guaranteed to be called
            // Stop - mirror onStart
            managedIabHelper.unsubscribe();
            // We won't be needing any lifecycle events if activity is finishing
            if (getActivity().isFinishing()) {
                OPFIab.unregister(this);
            }
        } else if (type == ComponentState.DETACH) {
            // Detach - fragment is removed, unsubscribe from everything
            managedIabHelper.unsubscribe();
            OPFIab.unregister(this);
        }
    }

    public void purchase(@NonNull final String sku) {
        purchase(getActivity(), sku);
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

    @SuppressFBWarnings({"ACEM_ABSTRACT_CLASS_EMPTY_METHODS"})
    @Override
    public final void onActivityResult(@NonNull final Activity activity,
                                       final int requestCode,
                                       final int resultCode,
                                       @Nullable final Intent data) {
        throw new UnsupportedOperationException("Activity result is handled automatically.");
    }
}
