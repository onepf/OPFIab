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
import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;

import org.onepf.opfiab.model.Configuration;
import org.onepf.opfutils.Checkable;
import org.onepf.opfutils.OPFChecks;
import org.onepf.opfutils.OPFLog;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import de.greenrobot.event.EventBus;

public final class OPFIab {

    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();
    private static final EventBus EVENT_BUS = EventBus.builder()
            .executorService(EXECUTOR)
            .throwSubscriberException(true)
            .eventInheritance(true)
            .build();
    private static final Checkable CHECK_INIT = new Checkable() {
        @Override
        public boolean check() {
            return baseIabHelper != null;
        }
    };

    private static volatile Configuration configuration;
    private static volatile Context context;

    private static BaseIabHelper baseIabHelper;


    private OPFIab() {
        throw new UnsupportedOperationException();
    }

    @NonNull
    static ExecutorService getExecutor() {
        return EXECUTOR;
    }

    @NonNull
    static EventBus getEventBus() {
        return EVENT_BUS;
    }

    @NonNull
    static BaseIabHelper getBaseHelper() {
        OPFChecks.checkThread(true);
        OPFChecks.checkInit(CHECK_INIT, true);
        return baseIabHelper;
    }

    @NonNull
    public static IabHelper getHelper() {
        return getBaseHelper();
    }

    @NonNull
    public static ManagedIabHelper getManagedHelper() {
        return new ManagedIabHelper(getBaseHelper());
    }

    @NonNull
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static SelfManagedIabHelper getHelper(@NonNull final Activity activity) {
        return new ActivityIabHelper(getManagedHelper(), activity);
    }

    @NonNull
    public static SelfManagedIabHelper getHelper(@NonNull final FragmentActivity fragmentActivity) {
        return new ActivityIabHelper(getManagedHelper(), fragmentActivity);
    }

    @NonNull
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    public static SelfManagedIabHelper getHelper(@NonNull final android.app.Fragment fragment) {
        return new FragmentIabHelper(getManagedHelper(), fragment);
    }

    @NonNull
    public static SelfManagedIabHelper getHelper(
            @NonNull final android.support.v4.app.Fragment fragment) {
        return new FragmentIabHelper(getManagedHelper(), fragment);
    }

    @NonNull
    public static Context getContext() {
        if (context == null) {
            throw new IllegalStateException();
        }
        return context;
    }

    @NonNull
    public static Configuration getConfiguration() {
        if (configuration == null) {
            throw new IllegalStateException();
        }
        return configuration;
    }

    public static void init(@NonNull final Context context,
                            @NonNull final Configuration configuration) {
        OPFChecks.checkThread(true);
        OPFChecks.checkInit(CHECK_INIT, false);
        OPFLog.init(OPFIab.class);
        OPFLog.methodD(context, configuration);
        OPFLog.d("Test log");
        OPFLog.d(null);
        OPFLog.d("Test formatted log: %s", "argument");
        OPFIab.context = context.getApplicationContext();
        OPFIab.configuration = configuration;
        EVENT_BUS.register(baseIabHelper = new BaseIabHelper(), Integer.MAX_VALUE);
        EVENT_BUS.register(new GlobalBillingListener(configuration.getBillingListener()));
    }

    public static void setup() {
        OPFChecks.checkThread(true);
        OPFChecks.checkInit(CHECK_INIT, true);
        SetupManager.setup();
    }
}
