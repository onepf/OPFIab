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

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.onepf.opfiab.misc.OPFIabFragment;
import org.onepf.opfiab.misc.OPFIabSupportFragment;
import org.onepf.opfiab.model.ComponentState;
import org.onepf.opfiab.model.event.FragmentLifecycleEvent;
import org.onepf.opfiab.model.event.SupportFragmentLifecycleEvent;

abstract class ComponentIabHelper extends AdvancedIabHelper {

    protected static final String FRAGMENT_TAG = "OPFIabFragment";

    @NonNull
    private final Object opfFragment;

    ComponentIabHelper(@Nullable final android.support.v4.app.FragmentManager supportFragmentManager,
                       @Nullable final android.app.FragmentManager fragmentManager) {
        super();
        OPFIab.register(this);

        if (supportFragmentManager != null) {
            final android.support.v4.app.Fragment existingFragment = supportFragmentManager
                    .findFragmentByTag(FRAGMENT_TAG);
            if (existingFragment != null) {
                opfFragment = existingFragment;
                register();
                return;
            }
            final android.support.v4.app.Fragment fragment = OPFIabSupportFragment.newInstance();
            opfFragment = fragment;
            supportFragmentManager.beginTransaction()
                    .add(fragment, FRAGMENT_TAG)
                    .commit();
            supportFragmentManager.executePendingTransactions();
            return;
        }

        if (fragmentManager != null) {
            final android.app.Fragment existingFragment = fragmentManager
                    .findFragmentByTag(FRAGMENT_TAG);
            if (existingFragment != null) {
                opfFragment = existingFragment;
                register();
                return;
            }
            final android.app.Fragment fragment = OPFIabFragment.newInstance();
            opfFragment = fragment;
            fragmentManager.beginTransaction()
                    .add(fragment, FRAGMENT_TAG)
                    .commit();
            fragmentManager.executePendingTransactions();
            return;
        }

        throw new IllegalStateException();
    }

    protected abstract void handleState(@NonNull final ComponentState type);

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
