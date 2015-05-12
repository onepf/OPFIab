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
import android.app.Fragment;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.onepf.opfiab.android.OPFIabFragment;
import org.onepf.opfiab.android.OPFIabSupportFragment;
import org.onepf.opfiab.api.IabHelper;
import org.onepf.opfiab.model.ComponentState;
import org.onepf.opfiab.model.event.android.FragmentLifecycleEvent;
import org.onepf.opfiab.model.event.android.SupportFragmentLifecycleEvent;
import org.onepf.opfutils.OPFLog;

/**
 * This class contains common code for all {@link IabHelper} implementations intended to use from
 * within Android components ({@link Activity}, {@link Fragment}).
 * <p>
 * Helper attempts to attach instance of {@link OPFIabFragment} to supplied fragment manager.
 * Fragment will monitor component lifecycle and report it to the library.
 */
abstract class ComponentIabHelper extends AdvancedIabHelperImpl {

    protected static final String FRAGMENT_TAG = "OPFIabFragment";

    @NonNull
    protected final Object opfFragment;

    ComponentIabHelper(
            @Nullable final android.support.v4.app.FragmentManager supportFragmentManager,
            @Nullable final android.app.FragmentManager fragmentManager) {
        super();
        // Register for lifecycle event right a way
        OPFIab.register(this);

        if (supportFragmentManager != null) {
            OPFLog.d("ComponentIabHelper uses android.support.v4.app.Fragment.");
            final android.support.v4.app.Fragment existingFragment = supportFragmentManager
                    .findFragmentByTag(FRAGMENT_TAG);
            if (existingFragment != null) {
                // Fragment already attached
                opfFragment = existingFragment;
                register();
                return;
            }
            final android.support.v4.app.Fragment fragment = OPFIabSupportFragment.newInstance();
            opfFragment = fragment;
            // Attach new fragment
            supportFragmentManager.beginTransaction()
                    .add(fragment, FRAGMENT_TAG)
                    .commit();
            // wait for onAttach() callback
            supportFragmentManager.executePendingTransactions();
            return;
        }

        if (fragmentManager != null) {
            OPFLog.d("ComponentIabHelper uses android.app.Fragment.");
            final Fragment existingFragment = fragmentManager
                    .findFragmentByTag(FRAGMENT_TAG);
            if (existingFragment != null) {
                opfFragment = existingFragment;
                register();
                return;
            }
            final Fragment fragment = OPFIabFragment.newInstance();
            opfFragment = fragment;
            fragmentManager.beginTransaction()
                    .add(fragment, FRAGMENT_TAG)
                    .commit();
            fragmentManager.executePendingTransactions();
            return;
        }

        throw new IllegalStateException();
    }

    /**
     * Gets activity from the associated android component.
     *
     * @return Activity object, can't be null.
     */
    @NonNull
    protected abstract Activity getActivity();

    /**
     * Handles reported lifecycle state.
     *
     * @param type Component state to handle.
     *
     * @see #register()
     * @see #unregister()
     */
    protected abstract void handleState(@NonNull final ComponentState type);

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    public void onEventMainThread(@NonNull final FragmentLifecycleEvent event) {
        if (opfFragment == event.getFragment()) {
            handleState(event.getType());
        }
    }

    public void onEventMainThread(@NonNull final SupportFragmentLifecycleEvent event) {
        if (opfFragment == event.getFragment()) {
            handleState(event.getType());
        }
    }
}
