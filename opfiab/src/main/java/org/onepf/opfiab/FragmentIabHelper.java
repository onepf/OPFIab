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
import android.support.annotation.Nullable;

import org.onepf.opfiab.model.event.FragmentLifecycleEvent;
import org.onepf.opfiab.model.event.SupportFragmentLifecycleEvent;

import static org.onepf.opfiab.model.event.LifecycleEvent.Type;

public class FragmentIabHelper extends SelfManagedIabHelper {

    @Nullable
    private final android.app.Fragment fragment;
    @Nullable
    private final android.support.v4.app.Fragment supportFragment;
    @NonNull
    private final ManagedIabHelper managedIabHelper;
    @NonNull
    private Object opfFragment;

    private FragmentIabHelper(@NonNull final ManagedIabHelper managedIabHelper,
                              @Nullable final android.app.Fragment fragment,
                              @Nullable final android.support.v4.app.Fragment supportFragment) {
        super(managedIabHelper);
        this.managedIabHelper = managedIabHelper;
        this.fragment = fragment;
        this.supportFragment = supportFragment;
        OPFIab.register(this);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    FragmentIabHelper(@NonNull final ManagedIabHelper managedIabHelper,
                      @NonNull final android.app.Fragment fragment) {
        this(managedIabHelper, fragment, null);

        android.app.Fragment opfFragment;
        final android.app.FragmentManager fragmentManager = fragment.getChildFragmentManager();
        if ((opfFragment = fragmentManager.findFragmentByTag(FRAGMENT_TAG)) == null) {
            opfFragment = OPFIabFragment.newInstance();
            fragmentManager.beginTransaction()
                    .add(opfFragment, FRAGMENT_TAG)
                    .commit();
        }
        this.opfFragment = opfFragment;
        fragmentManager.executePendingTransactions();
    }

    FragmentIabHelper(@NonNull final ManagedIabHelper managedIabHelper,
                      @NonNull final android.support.v4.app.Fragment supportFragment) {
        this(managedIabHelper, null, supportFragment);

        android.support.v4.app.Fragment opfFragment;
        final android.support.v4.app.FragmentManager fragmentManager = supportFragment.getChildFragmentManager();
        if ((opfFragment = fragmentManager.findFragmentByTag(FRAGMENT_TAG)) == null) {
            opfFragment = OPFIabSupportFragment.newInstance();
            fragmentManager.beginTransaction()
                    .add(opfFragment, FRAGMENT_TAG)
                    .commit();
        }
        this.opfFragment = opfFragment;
        fragmentManager.executePendingTransactions();
    }

    public void onEventMainThread(@NonNull final FragmentLifecycleEvent event) {
        if (opfFragment != event.getFragment()) {
            return;
        }
        handleLifecycle(event.getType());
    }

    public void onEventMainThread(@NonNull final SupportFragmentLifecycleEvent event) {
        if (opfFragment != event.getFragment()) {
            return;
        }
        handleLifecycle(event.getType());
    }

    private void handleLifecycle(@NonNull final Type type) {
        if (type == Type.ATTACH) {
            managedIabHelper.subscribe();
        } else if (type == Type.DETACH) {
            managedIabHelper.unsubscribe();
        } else if ((type == Type.PAUSE && getActivity().isFinishing()) || type == Type.DESTROY) {
            managedIabHelper.unsubscribe();
            OPFIab.unregister(this);
        }
    }

    @Override
    public void purchase(@NonNull final String sku) {
        managedIabHelper.purchase(getActivity(), sku);
    }

    @NonNull
    private Activity getActivity() {
        final Activity activity;
        if (supportFragment != null) {
            activity = supportFragment.getActivity();
        } else if (fragment != null) {
            activity = fragment.getActivity();
        } else {
            throw new IllegalStateException("Fragment is detached!");
        }
        return activity;
    }
}
