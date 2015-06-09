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
import android.app.Instrumentation;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.test.uiautomator.UiDevice;
import android.support.v4.app.FragmentActivity;

import org.junit.Assert;
import org.onepf.opfiab.OPFIab;
import org.onepf.opfiab.api.FragmentIabHelper;
import org.onepf.opfiab.api.IabHelper;
import org.onepf.opfiab.billing.BillingProvider;
import org.onepf.opfiab.model.BillingProviderInfo;
import org.onepf.opfiab.model.Configuration;
import org.onepf.opfiab.opfiab_uitest.R;
import org.onepf.opfiab.opfiab_uitest.manager.BillingManagerAdapter;
import org.onepf.opfiab.opfiab_uitest.manager.TestManager;
import org.onepf.opfiab.opfiab_uitest.util.MockBillingProviderBuilder;
import org.onepf.opfiab.opfiab_uitest.util.validators.AlwaysFailValidator;
import org.onepf.opfiab.opfiab_uitest.util.validators.PurchaseRequestValidator;
import org.onepf.opfiab.opfiab_uitest.util.validators.PurchaseResponseValidator;
import org.onepf.opfiab.opfiab_uitest.util.validators.SetupResponseValidator;
import org.onepf.opfiab.opfiab_uitest.util.validators.SetupStartedEventValidator;

/**
 * @author antonpp
 * @since 04.06.15
 */
public class UnifiedFragmentHelperTest {

    private static final String TEST_APP_PKG = "org.onepf.opfiab.opfiab_uitest";
    private static final String TEST_PROVIDER_PACKAGE = "org.onepf.opfiab.uitest";
    private static final String TEST_PROVIDER_NAME_FMT = "TEST_PROVIDER_NAME_%s";
    private static final String SKU_CONSUMABLE = "org.onepf.opfiab.consumable";
    private static final String SKU_NONCONSUMABLE = "org.onepf.opfiab.nonconsumable";
    private static final String SKU_SUBSCRIPTION = "org.onepf.opfiab.subscription";
    private static final long WAIT_BILLING_PROVIDER = 1000L;
    private static final long WAIT_PURCHASE = 2 * WAIT_BILLING_PROVIDER;
    private static final long WAIT_INIT = 2 * WAIT_BILLING_PROVIDER;
    private static final long WAIT_LAUNCH_SCREEN = 5000L;
    private static final long WAIT_REOPEN_ACTIVITY = 2000L;
    private static final long WAIT_TEST_MANAGER = 2 * WAIT_BILLING_PROVIDER;
    private static final Intent START_EMPTY_ACTIVITY = new Intent(Intent.ACTION_MAIN)
            .setComponent(new ComponentName(TEST_APP_PKG, TEST_APP_PKG + ".EmptyActivity"))
            .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);


    public static void testRegisterUnregisterHomeButton(Instrumentation instrumentation,
                                                        final Activity activity, UiDevice uiDevice)
            throws InterruptedException {
        final boolean isSupport = activity instanceof FragmentActivity;
        final Object fragment;
        if (isSupport) {
            fragment = SupportTestFragment.getInstance(R.color.blue);
            ((FragmentActivity) activity).getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.content, (android.support.v4.app.Fragment) fragment)
                    .commit();

        } else {
            fragment = TestFragment.getInstance(R.color.blue);
            activity.getFragmentManager()
                    .beginTransaction()
                    .replace(R.id.content, (Fragment) fragment)
                    .commit();
        }

        final String providerName = String.format(TEST_PROVIDER_NAME_FMT, "HOME");
        final BillingProvider billingProvider = new MockBillingProviderBuilder()
                .setIsAuthorised(true)
                .setWillPostSuccess(true)
                .setInfo(new BillingProviderInfo(providerName, TEST_PROVIDER_PACKAGE))
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

        changeToHomeScreen(uiDevice);

        purchase(instrumentation, helper, SKU_CONSUMABLE);
        Thread.sleep(WAIT_LAUNCH_SCREEN);

        reopenActivity(instrumentation);

        uiDevice.pressBack();

        purchase(instrumentation, helper, SKU_CONSUMABLE);
        Thread.sleep(WAIT_LAUNCH_SCREEN);

        purchaseListenerAdapter.validateEvent(AlwaysFailValidator.getStopObject());

        for (TestManager manager : managers) {
            Assert.assertTrue(manager.await(WAIT_TEST_MANAGER));
        }
    }

    private static void purchase(Instrumentation instrumentation, IabHelper helper, String sku) {
        instrumentation.runOnMainSync(new PurchaseRunnable(helper, sku));
    }

    private static void changeToHomeScreen(UiDevice uiDevice) throws InterruptedException {
        uiDevice.pressHome();
        Thread.sleep(WAIT_REOPEN_ACTIVITY);
    }

    private static void reopenActivity(Instrumentation instrumentation)
            throws InterruptedException {
        final Context context = instrumentation.getContext();
        final Intent intent = ((ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE))
                .getRecentTasks(2, 0).get(1).baseIntent;
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        instrumentation.getContext().startActivity(intent);
        Thread.sleep(WAIT_REOPEN_ACTIVITY);
    }

    public static void testRegisterUnregisterFragmentReplace(Instrumentation instrumentation,
                                                             final Activity activity,
                                                             UiDevice uiDevice)
            throws InterruptedException {
        final String providerName = String.format(TEST_PROVIDER_NAME_FMT, "FRAGMENT_REPLACE");
        final boolean isSupport = activity instanceof FragmentActivity;
        final Object fragment;
        if (isSupport) {
            fragment = SupportTestFragment.getInstance(R.color.green);
            ((FragmentActivity) activity).getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.content, (android.support.v4.app.Fragment) fragment)
                    .commit();

        } else {
            fragment = TestFragment.getInstance(R.color.green);
            activity.getFragmentManager()
                    .beginTransaction()
                    .replace(R.id.content, (Fragment) fragment)
                    .commit();
        }

        final BillingProvider billingProvider = new MockBillingProviderBuilder()
                .setIsAuthorised(true)
                .setWillPostSuccess(true)
                .setInfo(new BillingProviderInfo(providerName, TEST_PROVIDER_PACKAGE))
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
