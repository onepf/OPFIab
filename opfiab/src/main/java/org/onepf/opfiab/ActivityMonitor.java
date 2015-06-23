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
import android.app.Application;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.onepf.opfiab.model.ComponentState;
import org.onepf.opfutils.OPFChecks;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import static org.onepf.opfiab.model.ComponentState.CREATE;
import static org.onepf.opfiab.model.ComponentState.DESTROY;
import static org.onepf.opfiab.model.ComponentState.PAUSE;
import static org.onepf.opfiab.model.ComponentState.RESUME;
import static org.onepf.opfiab.model.ComponentState.START;
import static org.onepf.opfiab.model.ComponentState.STOP;

/**
 * This class is designed to monitor the existing {@link Activity}s lifecycle.
 * <p>
 * Intended for internal use.
 */
public final class ActivityMonitor implements Application.ActivityLifecycleCallbacks {

    /**
     * Map of the existing activities lifecycle states.
     * <p>
     * Backed up by {@link WeakHashMap} to avoid memory leaks.
     */
    @SuppressWarnings("Convert2Diamond")
    @SuppressFBWarnings({"PMB_POSSIBLE_MEMORY_BLOAT"})
    private static final Map<Activity, ComponentState> STATE_MAP =
            Collections.synchronizedMap(new WeakHashMap<Activity, ComponentState>());
    @Nullable
    private static ActivityMonitor instance;


    @SuppressWarnings({"PMD.NonThreadSafeSingleton"})
    public static Application.ActivityLifecycleCallbacks getInstance() {
        OPFChecks.checkThread(true);
        if (instance == null) {
            instance = new ActivityMonitor();
        }
        return instance;
    }

    /**
     * Sets lifecycle state of the supplied activity.
     *
     * @param activity Activity object to set lifecycle state for.
     */
    public static void setState(@NonNull final Activity activity,
                                @NonNull final ComponentState componentState) {
        STATE_MAP.put(activity, componentState);
    }

    /**
     * Gets last known lifecycle state of the supplied activity.
     *
     * @param activity Activity object to get lifecycle state for.
     *
     * @return Current lifecycle state if known, null otherwise.
     */
    @Nullable
    public static ComponentState getState(@NonNull final Activity activity) {
        return STATE_MAP.get(activity);
    }

    /**
     * Checks if supplied activity is in resumed state.
     *
     * @param activity Activity object to check lifecycle state for.
     *
     * @return True if supplied activity is resumed.
     *
     * @see {@link Activity#onResume()}
     */
    public static boolean isResumed(@NonNull final Activity activity) {
        return getState(activity) == RESUME;
    }

    public static boolean isStarted(@NonNull final Activity activity) {
        return Arrays.asList(RESUME, PAUSE, START).contains(getState(activity));
    }

    public static boolean isDestroyed(@NonNull final Activity activity) {
        return getState(activity) == DESTROY;
    }

    private ActivityMonitor() {
        super();
    }

    @Override
    public void onActivityCreated(final Activity activity, final Bundle savedInstanceState) {
        setState(activity, CREATE);
    }

    @Override
    public void onActivityStarted(final Activity activity) {
        setState(activity, START);
    }

    @Override
    public void onActivityResumed(final Activity activity) {
        setState(activity, RESUME);
    }

    @Override
    public void onActivityPaused(final Activity activity) {
        setState(activity, PAUSE);
    }

    @Override
    public void onActivityStopped(final Activity activity) {
        setState(activity, STOP);
    }

    @Override
    public void onActivitySaveInstanceState(final Activity activity, final Bundle outState) {
        // ignore
    }

    @Override
    public void onActivityDestroyed(final Activity activity) {
        setState(activity, DESTROY);
    }
}
