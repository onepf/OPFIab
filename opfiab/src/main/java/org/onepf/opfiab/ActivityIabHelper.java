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
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;

import org.onepf.opfiab.android.OPFIabFragment;
import org.onepf.opfiab.android.OPFIabSupportFragment;
import org.onepf.opfiab.model.billing.ConsumableDetails;
import org.onepf.opfiab.model.billing.EntitlementDetails;
import org.onepf.opfiab.model.billing.SubscriptionDetails;
import org.onepf.opfiab.model.event.FragmentLifecycleEvent;
import org.onepf.opfiab.model.event.SupportFragmentLifecycleEvent;

import static org.onepf.opfiab.OPFIab.FRAGMENT_TAG;

public class ActivityIabHelper extends SelfManagedIabHelper {

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
                    dispose();
                    managedIabHelper.unsubscribe();
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
        //TODO check attach\detach condition. Use sticky?
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

    @Override
    public void purchase(@NonNull final ConsumableDetails consumableDetails) {
        managedIabHelper.purchase(activity, consumableDetails);
    }

    @Override
    public void purchase(@NonNull final SubscriptionDetails subscriptionDetails) {
        managedIabHelper.purchase(activity, subscriptionDetails);
    }

    @Override
    public void purchase(@NonNull final EntitlementDetails entitlementDetails) {
        managedIabHelper.purchase(activity, entitlementDetails);
    }

    @Override
    protected void dispose() {
        eventBus.unregister(eventHandler);
    }
}
