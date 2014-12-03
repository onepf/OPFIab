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

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.onepf.opfiab.model.Options;

import de.greenrobot.event.EventBus;

public final class OPFIab {

    private OPFIab() {
        throw new UnsupportedOperationException();
    }

    private static BaseIabHelper baseIabHelper;

    private static EventBus eventBus;

    static final String FRAGMENT_TAG = "OPFIabFragment";

    @NonNull
    public static EventBus getEventBus() {
        OPFUtils.checkThread(true);
        if (eventBus == null) {
            eventBus = EventBus.builder()
                    .eventInheritance(true)
                    .build();
        }
        return eventBus;
    }

    @NonNull
    static BaseIabHelper getBaseHelper() {
        return baseIabHelper;
    }

    @NonNull
    public static IabHelper getHelper() {
        checkInit();
        OPFUtils.checkThread(true);
        return baseIabHelper;
    }

    @NonNull
    public static ManagedIabHelper getManagedHelper() {
        checkInit();
        OPFUtils.checkThread(true);
        return new ManagedIabHelper(baseIabHelper);
    }

    @NonNull
    public static ActivityIabHelper getHelper(@NonNull final Activity activity) {
        return new ActivityIabHelper(getManagedHelper(), activity);
    }

    @Nullable
    public static BillingProvider getCurrentProvider() {
        checkInit();
        return null;
    }

    public static void init(@NonNull final Context context, @NonNull final Options options) {
        OPFUtils.checkThread(true);
        if (baseIabHelper != null) {
            throw new IllegalStateException("init() was already called.");
        }
        baseIabHelper = new BaseIabHelper(context, options);
    }

    public static void setup() {

    }

    private static void checkInit() {
        if (baseIabHelper == null) {
            throw OPFUtils.notInitException();
        }
    }
}
