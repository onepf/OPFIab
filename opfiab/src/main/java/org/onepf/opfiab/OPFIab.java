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
import org.onepf.opfiab.model.event.SetupRequest;
import org.onepf.opfiab.model.event.SetupResponse;
import org.onepf.opfutils.OPFChecks;
import org.onepf.opfutils.exception.InitException;

import java.util.concurrent.Executors;

import de.greenrobot.event.EventBus;

public final class OPFIab {

    private static volatile Configuration configuration;
    private static volatile EventBus eventBus;
    private static volatile Context context;

    private static BaseIabHelper baseIabHelper;


    private OPFIab() {
        throw new UnsupportedOperationException();
    }

    private static void checkInit() {
        OPFChecks.checkThread(true);
        if (baseIabHelper == null) {
            throw new InitException(false);
        }
    }

    private static EventBus newBus() {
        return EventBus.builder()
                .executorService(Executors.newSingleThreadExecutor())
                .throwSubscriberException(true)
                .eventInheritance(true)
                .logNoSubscriberMessages(true)
                .build();
    }

    static void register(final Object subscriber) {
        eventBus.register(subscriber);
    }

    static void register(final Object subscriber, final int priority) {
        eventBus.register(subscriber, priority);
    }

    static void unregister(final Object subscriber) {eventBus.unregister(subscriber);}

    static void post(final Object event) {
        eventBus.post(event);
    }

    static void postSticky(final Object event) {
        eventBus.postSticky(event);
    }

    static <T> T getStickyEvent(final Class<T> eventType) {
        return eventBus.getStickyEvent(eventType);
    }

    static <T> T removeStickyEvent(final Class<T> eventType) {
        return eventBus.removeStickyEvent(eventType);
    }

    @NonNull
    static BaseIabHelper getBaseHelper() {
        checkInit();
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
        OPFIab.configuration = configuration;
        OPFIab.eventBus = newBus();
        OPFIab.context = context.getApplicationContext();
        OPFIab.baseIabHelper = new BaseIabHelper();
        register(baseIabHelper, Integer.MAX_VALUE);
        register(new SetupManager());
        register(new GlobalBillingListener(configuration.getBillingListener()));
    }

    public static void setup() {
        checkInit();
        if (getStickyEvent(SetupRequest.class) != null) {
            return;
        }
        postSticky(new SetupRequest(configuration));
    }
}
