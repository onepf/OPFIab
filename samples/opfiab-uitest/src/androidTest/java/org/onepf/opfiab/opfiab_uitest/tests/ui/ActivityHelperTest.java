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
import org.onepf.opfiab.api.IabHelper;
import org.onepf.opfiab.billing.BillingProvider;
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

import static org.onepf.opfiab.opfiab_uitest.util.Constants.SKU_CONSUMABLE;
import static org.onepf.opfiab.opfiab_uitest.util.Constants.TEST_APP_PKG;
import static org.onepf.opfiab.opfiab_uitest.util.Constants.TEST_PROVIDER_NAME;
import static org.onepf.opfiab.opfiab_uitest.util.Constants.WAIT_BILLING_PROVIDER;
import static org.onepf.opfiab.opfiab_uitest.util.Constants.WAIT_LAUNCH_SCREEN;
import static org.onepf.opfiab.opfiab_uitest.util.Constants.WAIT_PURCHASE;
import static org.onepf.opfiab.opfiab_uitest.util.Constants.WAIT_REOPEN_ACTIVITY;
import static org.onepf.opfiab.opfiab_uitest.util.Constants.WAIT_TEST_MANAGER;

/**
 * @author antonpp
 * @since 02.06.15
 */

public class ActivityHelperTest {

    private static final Intent START_EMPTY_ACTIVITY = new Intent(Intent.ACTION_MAIN)
            .setComponent(new ComponentName(TEST_APP_PKG, TEST_APP_PKG + ".EmptyActivity"))
            .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);

    @Rule
    public final ActivityTestRule<EmptyActivity> testRule = new ActivityTestRule<>(
            EmptyActivity.class);
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
                .setWillPostSuccess(true)
                .setName(TEST_PROVIDER_NAME)
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

        final TestManager[] managers = {testGlobalListenerManager, testSetupManager, testPurchaseManager};

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
            }
        });

        final ActivityIabHelper helper = helperArray[0];

        purchase(helper, SKU_CONSUMABLE);

        changeToHomeScreen();

        purchase(helper, SKU_CONSUMABLE, WAIT_LAUNCH_SCREEN);

        reopenActivity();

        setupListenerAdapter.validateEvent(AlwaysFailValidator.getStopObject());
        purchaseListenerAdapter.validateEvent(AlwaysFailValidator.getStopObject());

        purchase(helper, SKU_CONSUMABLE);

        for (TestManager manager : managers) {
            Assert.assertTrue(manager.await(WAIT_TEST_MANAGER));
        }
    }

    private void purchase(final IabHelper helper, final String skuConsumable)
            throws InterruptedException {
        purchase(helper, skuConsumable, WAIT_PURCHASE);
    }

    private void changeToHomeScreen() throws InterruptedException {
        uiDevice.pressHome();
        Thread.sleep(WAIT_REOPEN_ACTIVITY);
    }

    private void purchase(final IabHelper helper, final String skuConsumable, long timeout)
            throws InterruptedException {
        instrumentation.runOnMainSync(new Runnable() {
            @Override
            public void run() {
                OPFIab.setup();
                helper.purchase(SKU_CONSUMABLE);
            }
        });
        Thread.sleep(timeout);
    }

    private void reopenActivity() throws InterruptedException {
        final Context context = instrumentation.getContext();
        @SuppressWarnings("deprecation")
        final Intent intent = ((ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE))
                .getRecentTasks(2, 0).get(1).baseIntent;
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        instrumentation.getContext().startActivity(intent);
        Thread.sleep(WAIT_REOPEN_ACTIVITY);
    }
}
