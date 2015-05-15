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

package org.onepf.opfiab.model.event.android;

import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;

import org.onepf.opfiab.model.ComponentState;

/**
 * Event that indicates {@link Fragment} lifecycle state change.
 */
public class SupportFragmentLifecycleEvent extends LifecycleEvent {

    @NonNull
    private final Fragment fragment;

    public SupportFragmentLifecycleEvent(
            @NonNull final ComponentState type, @NonNull final Fragment fragment) {
        super(type);
        this.fragment = fragment;
    }

    /**
     * Gets support fragment associated with this event.
     *
     * @return SupportFragment object, can't be null.
     */
    @NonNull
    public Fragment getFragment() {
        return fragment;
    }
}
