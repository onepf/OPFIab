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
import android.app.Application;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;

import org.onepf.opfiab.api.ActivityIabHelper;
import org.onepf.opfiab.api.AdvancedIabHelper;
import org.onepf.opfiab.api.FragmentIabHelper;
import org.onepf.opfiab.api.SimpleIabHelper;
import org.onepf.opfiab.billing.BillingProvider;
import org.onepf.opfiab.model.Configuration;
import org.onepf.opfiab.model.event.SetupRequest;
import org.onepf.opfutils.OPFChecks;
import org.onepf.opfutils.OPFLog;
import org.onepf.opfutils.exception.InitException;

import java.util.Collection;
import java.util.concurrent.Executors;

import de.greenrobot.event.EventBus;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

public final class OPFIab {

    private static volatile EventBus eventBus;
    private static Configuration configuration;

    private OPFIab() {
        throw new UnsupportedOperationException();
    }

    private static void checkInit() {
        OPFChecks.checkThread(true);
        if (configuration == null) {
            throw new InitException(false);
        }
    }

    private static EventBus newBus() {
        return EventBus.builder()
                // Must use only one background thread
                .executorService(Executors.newSingleThreadExecutor())
                .throwSubscriberException(true)
                .eventInheritance(true)
                .logSubscriberExceptions(OPFLog.isEnabled())
                .build();
    }

    static void register(@NonNull final Object subscriber) {
        OPFChecks.checkThread(true);
        if (!eventBus.isRegistered(subscriber)) {
            eventBus.register(subscriber);
        }
    }

    static void register(@NonNull final Object subscriber, final int priority) {
        OPFChecks.checkThread(true);
        if (!eventBus.isRegistered(subscriber)) {
            eventBus.register(subscriber, priority);
        }
    }

    static void unregister(@NonNull final Object subscriber) {
        OPFChecks.checkThread(true);
        if (eventBus.isRegistered(subscriber)) {
            eventBus.unregister(subscriber);
        }
    }

    static void cancelEventDelivery(@NonNull final Object event) {
        OPFChecks.checkThread(true);
        eventBus.cancelEventDelivery(event);
    }

    /**
     * Post an event to deliver to all subscribers. Intend to be used by {@link org.onepf.opfiab.billing.BillingProvider} implementations.
     *
     * @param event Event object to deliver.
     */
    public static void post(@NonNull final Object event) {
        if (eventBus.hasSubscriberForEvent(event.getClass())) {
            eventBus.post(event);
        } else {
            OPFLog.d("Skipping event delivery: %s", event);
        }
    }

    @NonNull
    public static SimpleIabHelper getSimpleHelper() {
        checkInit();
        return new SimpleIabHelperImpl();
    }

    @NonNull
    public static AdvancedIabHelper getAdvancedHelper() {
        checkInit();
        return new AdvancedIabHelperImpl();
    }

    @NonNull
    public static ActivityIabHelper getActivityHelper(
            @NonNull final FragmentActivity fragmentActivity) {
        checkInit();
        return new ActivityIabHelperImpl(fragmentActivity, null);
    }

    @NonNull
    public static ActivityIabHelper getActivityHelper(@NonNull final Activity activity) {
        checkInit();
        return new ActivityIabHelperImpl(null, activity);
    }

    @NonNull
    public static FragmentIabHelper getFragmentHelper(
            @NonNull final android.support.v4.app.Fragment fragment) {
        checkInit();
        return new FragmentIabHelperImpl(fragment, null);
    }

    @NonNull
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    public static FragmentIabHelper getFragmentHelper(
            @NonNull final android.app.Fragment fragment) {
        checkInit();
        return new FragmentIabHelperImpl(null, fragment);
    }

    @NonNull
    public static Configuration getConfiguration() {
        checkInit();
        return configuration;
    }

    @SuppressFBWarnings({"LI_LAZY_INIT_UPDATE_STATIC"})
    // Avoid posting events asynchronously during initialization
    public static void init(@NonNull final Application application,
                            @NonNull final Configuration configuration) {
        OPFChecks.checkThread(true);

        // Check if manifest satisfies all billing providers.
        final Collection<BillingProvider> providers = configuration.getProviders();
        for (final BillingProvider provider : providers) {
            provider.checkManifest();
        }

        final BillingBase billingBase = BillingBase.getInstance();
        billingBase.setConfiguration(configuration);

        final BillingRequestScheduler scheduler = BillingRequestScheduler.getInstance();
        scheduler.dropQueue();

        final EventBus eventBus = OPFIab.eventBus;
        if (eventBus == null) {
            // first init
            OPFIab.eventBus = newBus();

            register(billingBase, Integer.MAX_VALUE);
            register(scheduler);
            register(SetupManager.getInstance(application));
            register(BillingEventDispatcher.getInstance());

            application.registerActivityLifecycleCallbacks(ActivityMonitor.getInstance());
        }

        OPFIab.configuration = configuration;
    }

    public static void setup() {
        checkInit();
        post(new SetupRequest(configuration));
    }
}
