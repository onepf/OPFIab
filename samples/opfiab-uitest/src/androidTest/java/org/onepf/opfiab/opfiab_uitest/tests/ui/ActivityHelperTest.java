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

import android.app.ActivityManager;
import android.app.Instrumentation;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;
import android.support.test.uiautomator.UiDevice;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.onepf.opfiab.OPFIab;
import org.onepf.opfiab.api.ActivityIabHelper;
import org.onepf.opfiab.billing.BillingProvider;
import org.onepf.opfiab.model.BillingProviderInfo;
import org.onepf.opfiab.model.Configuration;
import org.onepf.opfiab.opfiab_uitest.EmptyActivity;
import org.onepf.opfiab.opfiab_uitest.manager.BillingManagerAdapter;
import org.onepf.opfiab.opfiab_uitest.manager.TestManager;
import org.onepf.opfiab.opfiab_uitest.util.MockBillingProviderBuilder;
import org.onepf.opfiab.opfiab_uitest.util.validators.AlwaysFailValidator;
import org.onepf.opfiab.opfiab_uitest.util.validators.EventValidator;
import org.onepf.opfiab.opfiab_uitest.util.validators.PurchaseRequestValidator;
import org.onepf.opfiab.opfiab_uitest.util.validators.PurchaseResponseValidator;
import org.onepf.opfiab.opfiab_uitest.util.validators.SetupResponseValidator;
import org.onepf.opfiab.opfiab_uitest.util.validators.SetupStartedEventValidator;

/**
 * @author antonpp
 * @since 02.06.15
 */

public class ActivityHelperTest {

    private static final String TEST_APP_PKG = "org.onepf.opfiab.opfiab_uitest";
    private static final String TEST_PROVIDER_PACKAGE = "org.onepf.opfiab.uitest";
    private static final String TEST_PROVIDER_NAME = "TEST_PROVIDER_NAME";
    private static final String SKU_CONSUMABLE = "org.onepf.opfiab.consumable";
    private static final String SKU_NONCONSUMABLE = "org.onepf.opfiab.nonconsumable";
    private static final String SKU_SUBSCRIPTION = "org.onepf.opfiab.subscription";
    private static final long MAX_WAIT_TIME = 1000L;
    private static final long WAIT_LAUNCH_SCREEN = 5000L;
    private static final long WAIT_REOPEN_ACTIVITY = 500L;
    private static final long WAIT_BILLING_PROVIDER = 1000L;
    private static final Intent START_EMPTY_ACTIVITY = new Intent(Intent.ACTION_MAIN)
            .setComponent(new ComponentName(TEST_APP_PKG, TEST_APP_PKG + ".EmptyActivity"))
            .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);

    @Rule
    public ActivityTestRule<EmptyActivity> testRule = new ActivityTestRule<>(EmptyActivity.class);
    private Instrumentation instrumentation;
    private UiDevice uiDevice;
    private EmptyActivity activity;

    @Before
    public void setUp() {
        instrumentation = InstrumentationRegistry.getInstrumentation();
        activity = testRule.getActivity();
        uiDevice = UiDevice.getInstance(instrumentation);
    }

    @Test
    public void testRegisterUnregister() throws InterruptedException {
        final BillingProvider billingProvider = new MockBillingProviderBuilder()
                .setIsAuthorised(true)
                .setWillPostSuccess(true)
                .setInfo(new BillingProviderInfo(TEST_PROVIDER_NAME, TEST_PROVIDER_PACKAGE))
                .setIsAvailable(true)
                .setSleepTime(WAIT_BILLING_PROVIDER)
                .build();

        final TestManager testSetupManager = new TestManager.Builder()
                .expectEvent(new SetupStartedEventValidator())
                .expectEvent(new SetupResponseValidator(TEST_PROVIDER_NAME))
                .expectEvent(new AlwaysFailValidator())
                .expectEvent(new SetupStartedEventValidator())
                .expectEvent(new SetupResponseValidator(TEST_PROVIDER_NAME))
                .setTag("Setup")
                .build();
        final BillingManagerAdapter setupListenerAdapter = new BillingManagerAdapter(
                testSetupManager,
                false);

        final TestManager testPurchaseManager = new TestManager.Builder()
                .expectEvent(new PurchaseResponseValidator(TEST_PROVIDER_NAME, true))
                .expectEvent(new AlwaysFailValidator())
                .expectEvent(new PurchaseResponseValidator(TEST_PROVIDER_NAME, true))
                .setTag("Purchase")
                .build();
        final BillingManagerAdapter purchaseListenerAdapter = new BillingManagerAdapter(
                testPurchaseManager, false);

        final EventValidator[] validators = {
                new SetupStartedEventValidator(),
                new SetupResponseValidator(TEST_PROVIDER_NAME),
                new PurchaseRequestValidator(SKU_CONSUMABLE),
                new PurchaseResponseValidator(TEST_PROVIDER_NAME, true)
        };
        final TestManager testGlobalListenerManager = new TestManager.Builder()
                .expectEvents(validators)
                .expectEvents(validators)
                .expectEvents(validators)
                .setStrategy(TestManager.Strategy.UNORDERED_EVENTS)
                .setTag("Global")
                .build();

        final Configuration configuration = new Configuration.Builder()
                .addBillingProvider(billingProvider)
                .setBillingListener(new BillingManagerAdapter(testGlobalListenerManager, false))
                .build();

        final ActivityIabHelper[] helperArray = new ActivityIabHelper[1];
        instrumentation.runOnMainSync(new Runnable() {
            @Override
            public void run() {
                OPFIab.init(activity.getApplication(), configuration);
                final ActivityIabHelper helper = OPFIab.getActivityHelper(activity);
                helper.addSetupListener(setupListenerAdapter);
                helper.addPurchaseListener(purchaseListenerAdapter);
                helperArray[0] = helper;

                OPFIab.setup();
                helper.purchase(SKU_CONSUMABLE);
            }
        });
        final ActivityIabHelper helper = helperArray[0];
        final TestManager[] managers = {testGlobalListenerManager, testSetupManager, testPurchaseManager};

        Thread.sleep(MAX_WAIT_TIME);

        uiDevice.pressHome();

        instrumentation.runOnMainSync(new Runnable() {
            @Override
            public void run() {
                OPFIab.setup();
                helper.purchase(SKU_CONSUMABLE);
            }
        });

        Thread.sleep(WAIT_LAUNCH_SCREEN);
        reopenActivity();
        Thread.sleep(WAIT_REOPEN_ACTIVITY);

        setupListenerAdapter.validateEvent(AlwaysFailValidator.getStopObject());
        purchaseListenerAdapter.validateEvent(AlwaysFailValidator.getStopObject());

        instrumentation.runOnMainSync(new Runnable() {
            @Override
            public void run() {
                OPFIab.setup();
                helper.purchase(SKU_CONSUMABLE);
            }
        });

        for (TestManager manager : managers) {
            Assert.assertTrue(manager.await(MAX_WAIT_TIME));
        }
    }


    private void reopenActivity() {
        final Context context = instrumentation.getContext();
        final Intent intent = ((ActivityManager) context.getSystemService(
                Context.ACTIVITY_SERVICE))
                .getRecentTasks(2, 0).get(1).baseIntent;
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        instrumentation.getContext().startActivity(intent);
    }
}
