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

package org.onepf.opfiab.opfiab_uitest;

import android.support.test.annotation.UiThreadTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.onepf.opfiab.OPFIab;
import org.onepf.opfiab.api.ActivityIabHelper;
import org.onepf.opfiab.billing.BillingProvider;
import org.onepf.opfiab.model.BillingProviderInfo;
import org.onepf.opfiab.model.Configuration;
import org.onepf.opfiab.opfiab_uitest.util.MockBillingProviderBuilder;
import org.onepf.opfiab.opfiab_uitest.validators.EventValidator;
import org.onepf.opfiab.opfiab_uitest.validators.PurchaseResponseValidator;
import org.onepf.opfiab.opfiab_uitest.validators.SetupResponseValidator;
import org.onepf.opfiab.opfiab_uitest.validators.SetupStartedEventValidator;

import java.util.ArrayList;
import java.util.Collection;

/**
 * @author antonpp
 * @since 15.05.15
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class ActivityIabHelperTest {

    private static final long MAX_WAIT_TIME = 200L;
    private static final String SKU_CONSUMABLE = "org.onepf.opfiab.consumable";
    private static final String SKU_NONCONSUMABLE = "org.onepf.opfiab.nonconsumable";
    private static final String SKU_SUBSCRIPTION = "org.onepf.opfiab.subscription";

    private static final int NUM_TESTS = 100;
    @Rule
    public ActivityTestRule<ActivityHelperActivity> testRule = new ActivityTestRule<>(ActivityHelperActivity.class);
    private ActivityIabHelper iabHelper;
    private ActivityHelperActivity activity;

    @Before
    public void setUp() throws Exception {

        activity = testRule.getActivity();
        iabHelper = activity.getIabHelper();

        setupDexmaker();
    }

    private BillingProvider prepareMockProvider(String name) {
        return new MockBillingProviderBuilder()
                .setIsAuthorised(true)
                .setIsAvailable(true)
                .setInfo(new BillingProviderInfo(name, null))
                .build();
    }

    @UiThreadTest
    @Test
    public void testSimpleSetup() {
        final String name = "Absolutely random name";
        final BillingProvider billingProvider = prepareMockProvider(name);

        final TestManager testManager = new TestManager.Builder()
                .addEvent(new SetupStartedEventValidator())
                .addEvent(new SetupResponseValidator(name))
                .setResultListener(new TestManager.TestResultListener() {
                    @Override
                    public void onTestResult(boolean passed) {
                        Assert.assertTrue(passed);
                    }
                })
                .build();

        final Configuration configuration = new Configuration.Builder()
                .addBillingProvider(billingProvider)
                .setBillingListener(testManager)
                .build();

        testManager.startTest(MAX_WAIT_TIME);

        OPFIab.init(activity.getApplication(), configuration);

        OPFIab.setup();
    }

    @UiThreadTest
    @Test
    public void testPurchase() {
        final String name = "Absolutely random name";
        final BillingProvider billingProvider = prepareMockProvider(name);
        final Collection<EventValidator> eventValidators = new ArrayList<>(NUM_TESTS);
        for (int i = 0; i < NUM_TESTS; ++i) {
            eventValidators.add(new PurchaseResponseValidator(name));
        }
        final TestManager testManager = new TestManager.Builder()
                .addEvent(new SetupStartedEventValidator())
                .addEvent(new SetupResponseValidator(name))
                .addEvents(eventValidators)
                .setResultListener(new TestManager.TestResultListener() {
                    @Override
                    public void onTestResult(boolean passed) {
                        Assert.assertTrue(passed);
                    }
                })
                .build();

        final Configuration configuration = new Configuration.Builder()
                .addBillingProvider(billingProvider)
                .setBillingListener(testManager)
                .build();

        testManager.startTest(MAX_WAIT_TIME * NUM_TESTS);

        OPFIab.init(activity.getApplication(), configuration);
        OPFIab.setup();

        for (int i = 0; i < NUM_TESTS; ++i) {
            buyConsumable();
        }
    }

    private void buyConsumable() {
        iabHelper.purchase(SKU_CONSUMABLE);
    }

    private void buyNonconsumable() {
        iabHelper.purchase(SKU_NONCONSUMABLE);
    }

    private void buySubscription() {
        iabHelper.purchase(SKU_SUBSCRIPTION);
    }

    /**
     * Workaround for Mockito and JB-MR2 incompatibility to avoid
     * java.lang.IllegalArgumentException: dexcache == null
     *
     * @see <a href="https://code.google.com/p/dexmaker/issues/detail?id=2">
     * https://code.google.com/p/dexmaker/issues/detail?id=2</a>
     */
    private void setupDexmaker() {
        // Explicitly set the Dexmaker cache, so tests that use mockito work
        final String dexCache = activity.getCacheDir().getPath();
        System.setProperty("dexmaker.dexcache", dexCache);
    }
}
