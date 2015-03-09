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

import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.onepf.opfiab.model.event.FragmentLifecycleEvent;
import org.onepf.opfiab.model.event.SupportFragmentLifecycleEvent;

public class FragmentIabHelper extends SelfManagedIabHelper {

    @Nullable
    private final android.app.Fragment fragment;
    @Nullable
    private final android.support.v4.app.Fragment supportFragment;
    @NonNull
    private final Object opfFragment;

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private FragmentIabHelper(@NonNull final ManagedIabHelper managedIabHelper,
                              @Nullable final android.app.Fragment fragment,
                              @Nullable final android.support.v4.app.Fragment supportFragment) {
        super(managedIabHelper);
        this.fragment = fragment;
        this.supportFragment = supportFragment;
        if (supportFragment != null) {
            final android.support.v4.app.FragmentManager fragmentManager =
                    supportFragment.getChildFragmentManager();
            final android.support.v4.app.Fragment existingFragment;
            if ((existingFragment = fragmentManager.findFragmentByTag(FRAGMENT_TAG)) != null) {
                opfFragment = existingFragment;
                managedIabHelper.subscribe();
            } else {
                final android.support.v4.app.Fragment newFragment = OPFIabSupportFragment.newInstance();
                fragmentManager.beginTransaction()
                        .add(newFragment, FRAGMENT_TAG)
                        .commit();
                fragmentManager.executePendingTransactions();
                opfFragment = newFragment;
            }
        } else if (fragment != null) {
            final android.app.FragmentManager fragmentManager = fragment.getChildFragmentManager();
            final android.app.Fragment existingFragment;
            if ((existingFragment = fragmentManager.findFragmentByTag(FRAGMENT_TAG)) != null) {
                opfFragment = existingFragment;
                managedIabHelper.subscribe();
            } else {
                final android.app.Fragment newFragment = OPFIabFragment.newInstance();
                fragmentManager.beginTransaction()
                        .add(newFragment, FRAGMENT_TAG)
                        .commit();
                fragmentManager.executePendingTransactions();
                opfFragment = newFragment;
            }
        } else {
            throw new IllegalArgumentException("Fragment can't be null.");
        }
    }

    FragmentIabHelper(@NonNull final ManagedIabHelper managedIabHelper,
                      @NonNull final android.app.Fragment fragment) {
        this(managedIabHelper, fragment, null);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    FragmentIabHelper(@NonNull final ManagedIabHelper managedIabHelper,
                      @NonNull final android.support.v4.app.Fragment supportFragment) {
        this(managedIabHelper, null, supportFragment);
    }

    public void onEventMainThread(@NonNull final FragmentLifecycleEvent event) {
        if (opfFragment == event.getFragment()) {
            handleLifecycle(event.getType());
        }
    }

    public void onEventMainThread(@NonNull final SupportFragmentLifecycleEvent event) {
        if (opfFragment == event.getFragment()) {
            handleLifecycle(event.getType());
        }
    }

    @NonNull
    @Override
    protected Activity getActivity() {
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
