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
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;

import org.onepf.opfiab.model.event.FragmentLifecycleEvent;
import org.onepf.opfiab.model.event.SupportFragmentLifecycleEvent;

public class ActivityIabHelper extends SelfManagedIabHelper {

    @NonNull
    private final Activity activity;
    @NonNull
    private final Object opfFragment;

    private ActivityIabHelper(@NonNull final ManagedIabHelper managedIabHelper,
                              @Nullable final Activity activity,
                              @Nullable final FragmentActivity fragmentActivity) {
        super(managedIabHelper);
        if (fragmentActivity != null) {
            this.activity = fragmentActivity;
            final android.support.v4.app.FragmentManager fragmentManager =
                    fragmentActivity.getSupportFragmentManager();
            final android.support.v4.app.Fragment existingFragment;
            if ((existingFragment = fragmentManager.findFragmentByTag(FRAGMENT_TAG)) != null) {
                opfFragment = existingFragment;
                managedIabHelper.subscribe();
            } else {
                final android.support.v4.app.Fragment fragment = OPFIabSupportFragment.newInstance();
                fragmentManager.beginTransaction()
                        .add(fragment, FRAGMENT_TAG)
                        .commit();
                fragmentManager.executePendingTransactions();
                opfFragment = fragment;
            }
        } else if (activity != null) {
            this.activity = activity;
            final android.app.FragmentManager fragmentManager = activity.getFragmentManager();
            final android.app.Fragment existingFragment;
            if ((existingFragment = fragmentManager.findFragmentByTag(FRAGMENT_TAG)) != null) {
                opfFragment = existingFragment;
                managedIabHelper.subscribe();
            } else {
                final android.app.Fragment fragment = OPFIabFragment.newInstance();
                fragmentManager.beginTransaction()
                        .add(fragment, FRAGMENT_TAG)
                        .commit();
                fragmentManager.executePendingTransactions();
                opfFragment = fragment;
            }
        } else {
            throw new IllegalArgumentException("Activity can't be null.");
        }
    }

    ActivityIabHelper(@NonNull final ManagedIabHelper managedIabHelper,
                      @NonNull final Activity activity) {
        this(managedIabHelper, activity, null);
    }

    ActivityIabHelper(@NonNull final ManagedIabHelper managedIabHelper,
                      @NonNull final FragmentActivity fragmentActivity) {
        this(managedIabHelper, null, fragmentActivity);
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
        return activity;
    }
}
