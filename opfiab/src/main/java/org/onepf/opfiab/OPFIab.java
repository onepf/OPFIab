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
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;

import org.onepf.opfiab.api.ActivityIabHelper;
import org.onepf.opfiab.api.AdvancedIabHelper;
import org.onepf.opfiab.api.FragmentIabHelper;
import org.onepf.opfiab.api.IabHelper;
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

/**
 * This class is OPFIab library entry point.
 * <p/>
 * Before anything else, {@link #init(Application, Configuration)} must called.
 * Multiple init() calls are supported.
 * <p/>
 * Before utilizing library billing capabilities {@link BillingProvider} must be picked.
 * This might either be done with direct {@link #setup()} call or through the use of
 * {@link IabHelper} variant featuring lazy setup.
 * <p/>
 * To execute billing operations one must interact with {@link IabHelper}.
 *
 * @see #getSimpleHelper()
 * @see #getAdvancedHelper()
 * @see #getFragmentHelper(android.support.v4.app.Fragment)
 * @see #getFragmentHelper(android.app.Fragment)
 * @see #getActivityHelper(android.support.v4.app.FragmentActivity)
 * @see #getActivityHelper(Activity)
 */
public final class OPFIab {

    private static final EventBus EVENT_BUS = EventBus.builder()
            // Must use only one background thread
            .executorService(Executors.newSingleThreadExecutor())
            .throwSubscriberException(true)
            .eventInheritance(true)
            .logSubscriberExceptions(OPFLog.isEnabled())
            .build();

    private static Configuration configuration;

    private static void checkInit() {
        OPFChecks.checkThread(true);
        if (configuration == null) {
            throw new InitException(false);
        }
    }

    static void register(@NonNull final Object subscriber) {
        if (!EVENT_BUS.isRegistered(subscriber)) {
            EVENT_BUS.register(subscriber);
        }
    }

    static void register(@NonNull final Object subscriber, final int priority) {
        if (!EVENT_BUS.isRegistered(subscriber)) {
            EVENT_BUS.register(subscriber, priority);
        }
    }

    static void unregister(@NonNull final Object subscriber) {
        if (EVENT_BUS.isRegistered(subscriber)) {
            EVENT_BUS.unregister(subscriber);
        }
    }

    static void cancelEventDelivery(@NonNull final Object event) {
        EVENT_BUS.cancelEventDelivery(event);
    }

    /**
     * Posts event object for delivery to all subscribers.
     * Intend to be used by {@link BillingProvider} implementations.
     *
     * @param event Event object to deliver.
     */
    public static void post(@NonNull final Object event) {
        if (EVENT_BUS.hasSubscriberForEvent(event.getClass())) {
            EVENT_BUS.post(event);
        } else {
            OPFLog.d("Skipping event delivery: %s", event);
        }
    }

    /**
     * @return Simple version of {@link IabHelper}.
     * @see {@link SimpleIabHelper}
     */
    @NonNull
    public static SimpleIabHelper getSimpleHelper() {
        checkInit();
        return new SimpleIabHelperImpl();
    }

    /**
     * @return Feature reach version of {@link SimpleIabHelper}.
     * @see AdvancedIabHelper
     * @see #getSimpleHelper()
     */
    @NonNull
    public static AdvancedIabHelper getAdvancedHelper() {
        checkInit();
        return new AdvancedIabHelperImpl();
    }


    /**
     * Support version of {@link #getActivityHelper(Activity)}.
     */
    @NonNull
    public static ActivityIabHelper getActivityHelper(
            @NonNull final FragmentActivity fragmentActivity) {
        checkInit();
        return new ActivityIabHelperImpl(fragmentActivity, null);
    }

    /**
     * Instantiates {@link IabHelper} associated with supplied activity.
     * <p/>
     * This call will attach invisible fragment which will monitor activity lifecycle.
     * <p/>
     * Supplied activity <b>must</b> delegate {@link Activity#onActivityResult(int, int, Intent)}
     * to {@link ActivityIabHelper#onActivityResult(Activity, int, int, Intent)}.
     *
     * @param activity Activity object to associate helper with.
     * @return Version of {@link IabHelper} designed to be used from activity.
     * @see ActivityIabHelper
     */
    @NonNull
    public static ActivityIabHelper getActivityHelper(@NonNull final Activity activity) {
        checkInit();
        return new ActivityIabHelperImpl(null, activity);
    }

    /**
     * Support version of {@link #getFragmentHelper(android.app.Fragment)}.
     */
    @NonNull
    public static FragmentIabHelper getFragmentHelper(
            @NonNull final android.support.v4.app.Fragment fragment) {
        checkInit();
        return new FragmentIabHelperImpl(fragment, null);
    }

    /**
     * Instantiates {@link IabHelper} associated with supplied fragment.
     * <p/>
     * This call will attach invisible child fragment which will monitor parent lifecycle.
     * <p/>
     * If parent activity delegates {@link Activity#onActivityResult(int, int, Intent)}
     * to {@link ActivityIabHelper#onActivityResult(Activity, int, int, Intent)}, consider using
     * {@link SimpleIabHelper}.
     * <p/>
     * Nested fragments were introduced in Android API 17, use
     * {@link #getFragmentHelper(android.support.v4.app.Fragment)} for earlier versions.
     *
     * @param fragment Fragment object to associate helper with.
     * @return Version of {@link IabHelper} designed to be used from fragment.
     */
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

    /**
     * Initialize OPFIab library with supplied configuration.
     * <p/>
     * Subsequent init() calls are supported.
     *
     * @param application   Application object to add {@link Application.ActivityLifecycleCallbacks} to and to
     *                      use as {@link Context}.
     * @param configuration Configuration object to use.
     */
    @SuppressFBWarnings({"LI_LAZY_INIT_UPDATE_STATIC"})
    public static void init(@NonNull final Application application,
                            @NonNull final Configuration configuration) {
        OPFChecks.checkThread(true);

        // Check if manifest satisfies all billing providers.
        final Collection<BillingProvider> providers = configuration.getProviders();
        for (final BillingProvider provider : providers) {
            provider.checkManifest();
        }

        final BillingBase billingBase = BillingBase.getInstance();
        final BillingRequestScheduler scheduler = BillingRequestScheduler.getInstance();
        if (OPFIab.configuration == null) {
            // first init
            register(billingBase, Integer.MAX_VALUE);
            register(scheduler);
            register(SetupManager.getInstance(application));
            register(BillingEventDispatcher.getInstance());

            application.registerActivityLifecycleCallbacks(ActivityMonitor.getInstance());
        }

        scheduler.dropQueue();
        billingBase.setConfiguration(configuration);
        OPFIab.configuration = configuration;
    }

    /**
     * Try to pick one of the {@link BillingProvider}s supplied in {@link Configuration}.
     * <p/>
     * {@link #init(Application, Configuration)} must be called prior to this method.
     */
    public static void setup() {
        checkInit();
        post(new SetupRequest(configuration));
    }


    private OPFIab() {
        throw new UnsupportedOperationException();
    }
}
