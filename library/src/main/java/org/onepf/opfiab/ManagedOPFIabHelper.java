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

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.onepf.opfiab.listener.OnConsumeListener;
import org.onepf.opfiab.listener.OnInventoryListener;
import org.onepf.opfiab.listener.OnPurchaseListener;
import org.onepf.opfiab.listener.OnSetupListener;
import org.onepf.opfiab.listener.OnSkuInfoListener;
import org.onepf.opfiab.listener.OnSubscriptionListener;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class ManagedOPFIabHelper extends OPFIabHelperWrapper implements ManagedLifecycle {

    @NonNull
    final Set<OnSetupListener> setupListeners = Collections.synchronizedSet(
            new HashSet<OnSetupListener>());

    @NonNull
    final Set<OnPurchaseListener> purchaseListeners = Collections.synchronizedSet(
            new HashSet<OnPurchaseListener>());

    @NonNull
    final Set<OnSubscriptionListener> subscriptionListeners = Collections.synchronizedSet(
            new HashSet<OnSubscriptionListener>());

    @NonNull
    final Set<OnInventoryListener> inventoryListeners = Collections.synchronizedSet(
            new HashSet<OnInventoryListener>());

    @NonNull
    final Set<OnSkuInfoListener> skuInfoListeners = Collections.synchronizedSet(
            new HashSet<OnSkuInfoListener>());

    @NonNull
    final Set<OnConsumeListener> consumeListeners = Collections.synchronizedSet(
            new HashSet<OnConsumeListener>());

    @NonNull
    private final BillingListenerCompositor listenerCompositor = OPFIab.instance.billingListenerCompositor;

    public ManagedOPFIabHelper(final OPFIabHelper opfIabHelper) {
        super(opfIabHelper);
    }

    public void onCreate() {
    }

    public void onResume() {
        listenerCompositor.register(this);
    }

    public void onPause() {
        listenerCompositor.unregister(this);
    }

    @Override
    public boolean onActivityResult(final int requestCode, final int resultCode,
                                    @Nullable final Intent data) {
        return false;
    }
}
