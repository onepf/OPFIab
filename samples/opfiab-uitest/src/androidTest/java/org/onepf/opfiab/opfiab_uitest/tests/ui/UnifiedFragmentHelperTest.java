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

package org.onepf.opfiab.opfiab_uitest.tests.ui;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.support.test.uiautomator.UiDevice;
import android.support.v4.app.FragmentActivity;

import org.junit.Assert;
import org.onepf.opfiab.OPFIab;
import org.onepf.opfiab.api.FragmentIabHelper;
import org.onepf.opfiab.api.IabHelper;
import org.onepf.opfiab.billing.BillingProvider;
import org.onepf.opfiab.listener.OnPurchaseListener;
import org.onepf.opfiab.model.Configuration;
import org.onepf.opfiab.opfiab_uitest.EmptyActivity;
import org.onepf.opfiab.opfiab_uitest.EmptyFragmentActivity;
import org.onepf.opfiab.opfiab_uitest.R;
import org.onepf.opfiab.opfiab_uitest.manager.BillingManagerAdapter;
import org.onepf.opfiab.opfiab_uitest.manager.TestManager;
import org.onepf.opfiab.opfiab_uitest.util.MockBillingProviderBuilder;
import org.onepf.opfiab.opfiab_uitest.util.validators.AlwaysFailValidator;
import org.onepf.opfiab.opfiab_uitest.util.validators.PurchaseRequestValidator;
import org.onepf.opfiab.opfiab_uitest.util.validators.PurchaseResponseValidator;
import org.onepf.opfiab.opfiab_uitest.util.validators.SetupResponseValidator;
import org.onepf.opfiab.opfiab_uitest.util.validators.SetupStartedEventValidator;

import static org.onepf.opfiab.opfiab_uitest.util.Constants.SKU_CONSUMABLE;
import static org.onepf.opfiab.opfiab_uitest.util.Constants.TEST_PROVIDER_NAME_FMT;
import static org.onepf.opfiab.opfiab_uitest.util.Constants.WAIT_BILLING_PROVIDER;
import static org.onepf.opfiab.opfiab_uitest.util.Constants.WAIT_INIT;
import static org.onepf.opfiab.opfiab_uitest.util.Constants.WAIT_LAUNCH_SCREEN;
import static org.onepf.opfiab.opfiab_uitest.util.Constants.WAIT_PURCHASE;
import static org.onepf.opfiab.opfiab_uitest.util.Constants.WAIT_REOPEN_ACTIVITY;
import static org.onepf.opfiab.opfiab_uitest.util.Constants.WAIT_TEST_MANAGER;

/**
 * @author antonpp
 * @since 04.06.15
 */
public class UnifiedFragmentHelperTest {

    private final static String FRAGMENT_TAG = "FRAGMENT_TAG";

    public static void testRegisterUnregisterHomeButton(Instrumentation instrumentation,
                                                        final Activity activity, UiDevice uiDevice)
            throws InterruptedException {

        final String providerName = String.format(TEST_PROVIDER_NAME_FMT, "HOME");
        final BillingProvider billingProvider = new MockBillingProviderBuilder()
                .setWillPostSuccess(true)
                .setName(providerName)
                .setIsAvailable(true)
                .setSleepTime(WAIT_BILLING_PROVIDER)
                .build();

        final TestManager testPurchaseManager = new TestManager.Builder()
                .expectEvent(new PurchaseResponseValidator(providerName, true))
                .expectEvent(new PurchaseResponseValidator(providerName, true))
                .expectEvent(new AlwaysFailValidator())
                .setTag("Purchase")
                .setSkipWrongEvents(false)
                .build();
        final BillingManagerAdapter purchaseListenerAdapter = new BillingManagerAdapter(
                testPurchaseManager, false);

        final TestManager testGlobalListenerManager = new TestManager.Builder()
                .expectEvent(new SetupStartedEventValidator())
                .expectEvent(new SetupResponseValidator(providerName))
                .expectEvent(new PurchaseRequestValidator(SKU_CONSUMABLE))
                .expectEvent(new PurchaseResponseValidator(providerName, true))
                .expectEvent(new PurchaseRequestValidator(SKU_CONSUMABLE))
                .expectEvent(new PurchaseResponseValidator(providerName, true))
                .expectEvent(new PurchaseRequestValidator(SKU_CONSUMABLE))
                .expectEvent(new PurchaseResponseValidator(providerName, true))
                .setStrategy(TestManager.Strategy.UNORDERED_EVENTS)
                .setTag("Global")
                .build();

        final TestManager[] managers = {testGlobalListenerManager, testPurchaseManager};

        final Configuration configuration = new Configuration.Builder()
                .addBillingProvider(billingProvider)
                .setBillingListener(new BillingManagerAdapter(testGlobalListenerManager, false))
                .build();

        instrumentation.runOnMainSync(new Runnable() {
            @Override
            public void run() {
                OPFIab.init(activity.getApplication(), configuration);
            }
        });
        Thread.sleep(WAIT_INIT);
        final boolean isSupport = activity instanceof FragmentActivity;
        Object fragment;
        if (isSupport) {
            fragment = SupportTestFragment.getInstance(R.color.blue);
            final android.support.v4.app.FragmentManager supportFragmentManager = ((FragmentActivity) activity).getSupportFragmentManager();
            supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.content, (android.support.v4.app.Fragment) fragment, FRAGMENT_TAG)
                    .commit();
            instrumentation.runOnMainSync(new Runnable() {
                @Override
                public void run() {
                    supportFragmentManager.executePendingTransactions();
                }
            });
        } else {
            fragment = TestFragment.getInstance(R.color.blue);
            final FragmentManager fragmentManager = activity.getFragmentManager();
            fragmentManager
                    .beginTransaction()
                    .replace(R.id.content, (Fragment) fragment, FRAGMENT_TAG)
                    .commit();
            instrumentation.runOnMainSync(new Runnable() {
                @Override
                public void run() {
                    fragmentManager.executePendingTransactions();
                }
            });
        }
        Thread.sleep(WAIT_INIT);

        FragmentIabHelper helper = getHelper(isSupport, fragment, purchaseListenerAdapter,
                instrumentation);

        purchase(instrumentation, helper, SKU_CONSUMABLE);

        changeToHomeScreen(uiDevice);

        purchase(instrumentation, helper, SKU_CONSUMABLE);

        reopenActivity(instrumentation);

        fragment = getFragment(isSupport);
        helper = getHelper(isSupport, fragment, purchaseListenerAdapter, instrumentation);

        pressBackButton(uiDevice);

        purchase(instrumentation, helper, SKU_CONSUMABLE);

        purchaseListenerAdapter.validateEvent(AlwaysFailValidator.getStopObject());

        for (TestManager manager : managers) {
            Assert.assertTrue(manager.await(WAIT_TEST_MANAGER));
        }
    }

    private static FragmentIabHelper getHelper(final boolean isSupport, final Object fragment,
                                               final OnPurchaseListener listener,
                                               Instrumentation instrumentation)
            throws InterruptedException {
        final FragmentIabHelper[] helpers = new FragmentIabHelper[1];
        instrumentation.runOnMainSync(new Runnable() {
            @Override
            public void run() {
                if (isSupport) {
                    helpers[0] = ((SupportTestFragment) fragment).getIabHelper(listener);
                } else {
                    helpers[0] = ((TestFragment) fragment).getIabHelper(listener);
                }
            }
        });
        Thread.sleep(WAIT_INIT);
        return helpers[0];
    }

    private static void purchase(Instrumentation instrumentation, IabHelper helper, String sku)
            throws InterruptedException {
        purchase(instrumentation, helper, sku, WAIT_PURCHASE);
    }

    private static void changeToHomeScreen(UiDevice uiDevice) throws InterruptedException {
        uiDevice.pressHome();
        Thread.sleep(WAIT_REOPEN_ACTIVITY);
    }

    private static void reopenActivity(Instrumentation instrumentation)
            throws InterruptedException {
        final Context context = instrumentation.getContext();
        @SuppressWarnings("deprecation")
        final Intent intent = ((ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE))
                .getRecentTasks(2, 0).get(1).baseIntent;
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        instrumentation.getContext().startActivity(intent);
        Thread.sleep(WAIT_REOPEN_ACTIVITY);
    }

    private static Object getFragment(final boolean isSupport) {
        if (isSupport) {

            return EmptyFragmentActivity.getLastInstance().getSupportFragmentManager()
                    .findFragmentByTag(FRAGMENT_TAG);
        } else {
            return EmptyActivity.getLastInstance().getFragmentManager()
                    .findFragmentByTag(FRAGMENT_TAG);
        }
    }

    private static void pressBackButton(UiDevice uiDevice) throws InterruptedException {
        uiDevice.pressBack();
        Thread.sleep(WAIT_REOPEN_ACTIVITY);
    }

    private static void purchase(Instrumentation instrumentation, IabHelper helper, String sku,
                                 long timeout)
            throws InterruptedException {
        instrumentation.runOnMainSync(new PurchaseRunnable(helper, sku));
        Thread.sleep(timeout);
    }

    public static void testRegisterUnregisterFragmentReplace(Instrumentation instrumentation,
                                                             final Boolean isSupport,
                                                             UiDevice uiDevice)
            throws InterruptedException {
        final String providerName = String.format(TEST_PROVIDER_NAME_FMT, "FRAGMENT_REPLACE");
        final Object fragment;
        final Activity activity;
        if (isSupport) {
            fragment = SupportTestFragment.getInstance(R.color.green);
            activity = EmptyFragmentActivity.getLastInstance();
            ((FragmentActivity) activity).getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.content, (android.support.v4.app.Fragment) fragment)
                    .commit();

        } else {
            fragment = TestFragment.getInstance(R.color.green);
            activity = EmptyActivity.getLastInstance();
            activity.getFragmentManager()
                    .beginTransaction()
                    .replace(R.id.content, (Fragment) fragment)
                    .commit();
        }

        final BillingProvider billingProvider = new MockBillingProviderBuilder()
                .setWillPostSuccess(true)
                .setName(providerName)
                .setIsAvailable(true)
                .setSleepTime(WAIT_BILLING_PROVIDER)
                .build();

        final TestManager testPurchaseManager = new TestManager.Builder()
                .expectEvent(new PurchaseResponseValidator(providerName, true))
                .expectEvent(new AlwaysFailValidator())
                .expectEvent(new PurchaseResponseValidator(providerName, true))
                .setTag("Purchase")
                .build();
        final BillingManagerAdapter purchaseListenerAdapter = new BillingManagerAdapter(
                testPurchaseManager, false);

        final TestManager testGlobalListenerManager = new TestManager.Builder()
                .expectEvent(new SetupStartedEventValidator())
                .expectEvent(new SetupResponseValidator(providerName))
                .expectEvent(new PurchaseRequestValidator(SKU_CONSUMABLE))
                .expectEvent(new PurchaseResponseValidator(providerName, true))
                .expectEvent(new PurchaseRequestValidator(SKU_CONSUMABLE))
                .expectEvent(new PurchaseResponseValidator(providerName, true))
                .expectEvent(new PurchaseRequestValidator(SKU_CONSUMABLE))
                .expectEvent(new PurchaseResponseValidator(providerName, true))
                .setStrategy(TestManager.Strategy.UNORDERED_EVENTS)
                .setTag("Global")
                .build();

        final Configuration configuration = new Configuration.Builder()
                .addBillingProvider(billingProvider)
                .setBillingListener(new BillingManagerAdapter(testGlobalListenerManager, false))
                .build();

        final TestManager[] managers = {testGlobalListenerManager, testPurchaseManager};

        final FragmentIabHelper[] helpers = new FragmentIabHelper[1];
        instrumentation.runOnMainSync(new Runnable() {
            @Override
            public void run() {
                OPFIab.init(activity.getApplication(), configuration);
                if (isSupport) {
                    helpers[0] = OPFIab.getFragmentHelper(
                            (android.support.v4.app.Fragment) fragment);
                } else {
                    helpers[0] = OPFIab.getFragmentHelper((Fragment) fragment);
                }
                helpers[0].addPurchaseListener(purchaseListenerAdapter);
            }
        });
        Thread.sleep(WAIT_INIT);
        final FragmentIabHelper helper = helpers[0];

        purchase(instrumentation, helper, SKU_CONSUMABLE);
        Thread.sleep(WAIT_PURCHASE);

        replaceFragment(activity);

        purchase(instrumentation, helper, SKU_CONSUMABLE);
        Thread.sleep(WAIT_LAUNCH_SCREEN);

        restoreFragment(activity);

        purchaseListenerAdapter.validateEvent(AlwaysFailValidator.getStopObject());

        purchase(instrumentation, helper, SKU_CONSUMABLE);
        Thread.sleep(WAIT_PURCHASE);

        for (TestManager manager : managers) {
            Assert.assertTrue(manager.await(WAIT_TEST_MANAGER));
        }
    }

    private static void replaceFragment(Activity activity) throws InterruptedException {
        if (activity instanceof FragmentActivity) {
            ((FragmentActivity) activity).getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.content, SupportTestFragment.getInstance(R.color.red))
                    .addToBackStack(null)
                    .commit();

        } else {
            activity.getFragmentManager()
                    .beginTransaction()
                    .replace(R.id.content, TestFragment.getInstance(R.color.red))
                    .addToBackStack(null)
                    .commit();
        }
        Thread.sleep(WAIT_REOPEN_ACTIVITY);
    }

    private static void restoreFragment(Activity activity) throws InterruptedException {
        if (activity instanceof FragmentActivity) {
            ((FragmentActivity) activity).getSupportFragmentManager().popBackStack();
        } else {
            activity.getFragmentManager().popBackStack();
        }
        Thread.sleep(WAIT_REOPEN_ACTIVITY);
    }

    private static final class PurchaseRunnable implements Runnable {

        private final IabHelper helper;
        private final String sku;

        private PurchaseRunnable(final IabHelper helper, final String sku) {
            this.helper = helper;
            this.sku = sku;
        }

        @Override
        public void run() {
            helper.purchase(sku);
        }
    }
}
