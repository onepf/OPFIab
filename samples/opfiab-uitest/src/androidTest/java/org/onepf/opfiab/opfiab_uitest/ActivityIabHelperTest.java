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

import android.app.Instrumentation;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.onepf.opfiab.OPFIab;
import org.onepf.opfiab.api.ActivityIabHelper;
import org.onepf.opfiab.api.IabHelper;
import org.onepf.opfiab.billing.BillingProvider;
import org.onepf.opfiab.model.BillingProviderInfo;
import org.onepf.opfiab.model.Configuration;
import org.onepf.opfiab.opfiab_uitest.manager.TestManager;
import org.onepf.opfiab.opfiab_uitest.util.MockBillingProviderBuilder;
import org.onepf.opfiab.opfiab_uitest.validators.EventValidator;
import org.onepf.opfiab.opfiab_uitest.validators.PurchaseRequestValidator;
import org.onepf.opfiab.opfiab_uitest.validators.PurchaseResponseValidator;
import org.onepf.opfiab.opfiab_uitest.validators.SetupResponseValidator;
import org.onepf.opfiab.opfiab_uitest.validators.SetupStartedEventValidator;

import java.util.ArrayList;
import java.util.Collection;

import static junit.framework.Assert.assertTrue;

/**
 * @author antonpp
 * @since 15.05.15
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class ActivityIabHelperTest {

    private static final long MAX_WAIT_TIME = 1000L;
    private static final String SKU_CONSUMABLE = "org.onepf.opfiab.consumable";
    private static final String SKU_NONCONSUMABLE = "org.onepf.opfiab.nonconsumable";
    private static final String SKU_SUBSCRIPTION = "org.onepf.opfiab.subscription";

    private static final int NUM_TESTS = 10;

    @Rule
    public ActivityTestRule<EmptyActivity> testRule = new ActivityTestRule<>(EmptyActivity.class);

    private ActivityIabHelper iabHelper;
    private EmptyActivity activity;
    private Instrumentation instrumentation;

    @Before
    public void setUp() throws Exception {
        activity = testRule.getActivity();
        setupDexmaker();
        instrumentation = InstrumentationRegistry.getInstrumentation();
    }

    private BillingProvider prepareMockProvider(String name) {
        return new MockBillingProviderBuilder()
                .setIsAuthorised(true)
                .setIsAvailable(true)
                .setInfo(new BillingProviderInfo(name, null))
                .build();
    }

    private void initSetup(final Configuration configuration) {
        OPFIab.init(activity.getApplication(), configuration);
        iabHelper = OPFIab.getActivityHelper(activity);
        activity.setHelper(iabHelper);
        OPFIab.setup();
    }

    @Test
    public void testPurchase() throws InterruptedException {
        final String name = "Absolutely random name";
        final BillingProvider billingProvider = prepareMockProvider(name);
        final Collection<EventValidator> eventValidators = new ArrayList<>(NUM_TESTS);
        eventValidators.add(new PurchaseRequestValidator(SKU_CONSUMABLE));
        eventValidators.add(new PurchaseResponseValidator(name));

        final TestManager testManager = new TestManager.Builder()
                .expectEvent(new SetupStartedEventValidator())
                .expectEvent(new SetupResponseValidator(name))
                .expectEvents(eventValidators)
                .setStrategy(TestManager.Strategy.UNORDERED_EVENTS)
                .build();

        final Configuration configuration = new Configuration.Builder()
                .addBillingProvider(billingProvider)
                .setBillingListener(testManager)
                .build();

        instrumentation.runOnMainSync(new Runnable() {
            @Override
            public void run() {

                initSetup(configuration);

                final IabHelper iabHelper = OPFIab.getAdvancedHelper();

                for (int i = 0; i < NUM_TESTS; ++i) {
                    iabHelper.purchase(SKU_CONSUMABLE);
                }
            }
        });

        assertTrue(testManager.await(MAX_WAIT_TIME * NUM_TESTS));
    }

    @Test
    public void testSimpleSetup() throws InterruptedException {
        final String name = "Absolutely random name";
        final BillingProvider billingProvider = prepareMockProvider(name);

        final TestManager testManager = new TestManager.Builder()
                .expectEvent(new SetupStartedEventValidator())
                .expectEvent(new SetupResponseValidator(name))
                .build();

        instrumentation.runOnMainSync(new Runnable() {
            @Override
            public void run() {

                final Configuration configuration = new Configuration.Builder()
                        .addBillingProvider(billingProvider)
                        .setBillingListener(testManager)
                        .build();
                OPFIab.init(activity.getApplication(), configuration);
                iabHelper = OPFIab.getActivityHelper(activity);
                activity.setHelper(iabHelper);
                OPFIab.setup();
            }
        });

        assertTrue(testManager.await(MAX_WAIT_TIME));
    }

    @Test
    public void testMultiSetup() throws InterruptedException {
        final String[] bpNames = new String[NUM_TESTS];
        for (int i = 0; i < NUM_TESTS; ++i) {
            bpNames[i] = String.format("BillingProviderN%d", i);
        }
        final TestManager.Builder builder = new TestManager.Builder()
                .expectEvent(new SetupResponseValidator(bpNames[NUM_TESTS - 1]));

        final TestManager testManager = builder.build();

        instrumentation.runOnMainSync(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < NUM_TESTS; ++i) {
                    initSetup(new Configuration.Builder()
                                      .addBillingProvider(prepareMockProvider(bpNames[i]))
                                      .setBillingListener(testManager)
                                      .build()
                    );
                }
            }
        });

        assertTrue(testManager.await(MAX_WAIT_TIME * NUM_TESTS));
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
