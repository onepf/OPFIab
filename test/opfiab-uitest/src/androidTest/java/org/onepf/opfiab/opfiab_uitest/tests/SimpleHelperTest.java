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

package org.onepf.opfiab.opfiab_uitest.tests;

import android.app.Instrumentation;
import android.support.annotation.NonNull;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.onepf.opfiab.OPFIab;
import org.onepf.opfiab.billing.BillingProvider;
import org.onepf.opfiab.model.Configuration;
import org.onepf.opfiab.model.event.SetupResponse;
import org.onepf.opfiab.model.event.billing.PurchaseResponse;
import org.onepf.opfiab.opfiab_uitest.EmptyActivity;
import org.onepf.opfiab.opfiab_uitest.manager.BillingManagerAdapter;
import org.onepf.opfiab.opfiab_uitest.manager.TestManager;
import org.onepf.opfiab.opfiab_uitest.util.MockBillingProviderBuilder;
import org.onepf.opfiab.opfiab_uitest.util.validators.PurchaseRequestValidator;
import org.onepf.opfiab.opfiab_uitest.util.validators.PurchaseResponseValidator;
import org.onepf.opfiab.opfiab_uitest.util.validators.SetupResponseValidator;
import org.onepf.opfiab.opfiab_uitest.util.validators.SetupStartedEventValidator;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static junit.framework.Assert.assertTrue;
import static org.onepf.opfiab.opfiab_uitest.util.Constants.SKU_CONSUMABLE;
import static org.onepf.opfiab.opfiab_uitest.util.Constants.SKU_ENTITY;
import static org.onepf.opfiab.opfiab_uitest.util.Constants.SKU_SUBSCRIPTION;

/**
 * @author antonpp
 * @since 29.05.15
 */
public class SimpleHelperTest {


    private static final long MAX_WAIT_TIME = 2000L;

    private static final int NUM_TESTS = 10;

    @Rule
    public final ActivityTestRule<EmptyActivity> testRule = new ActivityTestRule<>(
            EmptyActivity.class);

    private EmptyActivity activity;
    private Instrumentation instrumentation;
    private CountDownLatch setupLatch;
    private CountDownLatch purchaseLatch;

    @Before
    public void setUp() {
        activity = testRule.getActivity();
        setupDexmaker();
        instrumentation = InstrumentationRegistry.getInstrumentation();
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

    @Test
    public void testSuccessfulPurchase() throws Exception {
        final String name = "Absolutely random name";
        final BillingProvider billingProvider = new MockBillingProviderBuilder()
                .setWillPostSuccess(true)
                .setName(name)
                .setIsAvailable(true)
                .build();

        final TestManager testManager = new TestManager.Builder()
                .expectEvent(new SetupStartedEventValidator())
                .expectEvent(new SetupResponseValidator(name))
                .expectEvent(new PurchaseRequestValidator(SKU_CONSUMABLE))
                .expectEvent(new PurchaseResponseValidator(name, true))
                .setStrategy(TestManager.Strategy.UNORDERED_EVENTS)
                .build();

        final Configuration configuration = new Configuration.Builder()
                .addBillingProvider(billingProvider)
                .setBillingListener(new MyAdapter(testManager))
                .build();

        setupLatch = new CountDownLatch(1);
        instrumentation.runOnMainSync(new SetConfig(configuration));
        instrumentation.runOnMainSync(new SetupLib());
        setupLatch.await(MAX_WAIT_TIME, TimeUnit.MILLISECONDS);
        instrumentation.runOnMainSync(new PurchaseSKU(SKU_CONSUMABLE));

        assertTrue(testManager.await(MAX_WAIT_TIME));
    }

    @Test
    public void testFailPurchase() throws Exception {
        final String name = "Absolutely random name";
        final BillingProvider billingProvider = new MockBillingProviderBuilder()
                .setWillPostSuccess(false)
                .setName(name)
                .setIsAvailable(true)
                .build();

        final TestManager testManager = new TestManager.Builder()
                .expectEvent(new SetupStartedEventValidator())
                .expectEvent(new SetupResponseValidator(name))
                .expectEvent(new PurchaseRequestValidator(SKU_CONSUMABLE))
                .expectEvent(new PurchaseResponseValidator(name, false))
                .setStrategy(TestManager.Strategy.UNORDERED_EVENTS)
                .build();

        final Configuration configuration = new Configuration.Builder()
                .addBillingProvider(billingProvider)
                .setBillingListener(new MyAdapter(testManager))
                .build();

        setupLatch = new CountDownLatch(1);
        instrumentation.runOnMainSync(new SetConfig(configuration));
        instrumentation.runOnMainSync(new SetupLib());
        setupLatch.await(MAX_WAIT_TIME, TimeUnit.MILLISECONDS);
        instrumentation.runOnMainSync(new PurchaseSKU(SKU_CONSUMABLE));


        assertTrue(testManager.await(MAX_WAIT_TIME));
    }

    @Test
    public void testMultiplePurchases() throws InterruptedException {
        final String billingProviderName1 = "BP 1";
        final String billingProviderName2 = "BP 2";
        final BillingProvider billingProvider1 = new MockBillingProviderBuilder()
                .setWillPostSuccess(false)
                .setName(billingProviderName1)
                .setIsAvailable(true)
                .build();

        final BillingProvider billingProvider2 = new MockBillingProviderBuilder()
                .setWillPostSuccess(true)
                .setName(billingProviderName2)
                .setIsAvailable(true)
                .build();

        final TestManager testManager = new TestManager.Builder()
                .expectEvent(new SetupStartedEventValidator())
                .expectEvent(new SetupResponseValidator(billingProviderName1))
                .expectEvent(new SetupStartedEventValidator())
                .expectEvent(new SetupResponseValidator(billingProviderName2))
                .expectEvent(new PurchaseRequestValidator(SKU_CONSUMABLE))
                .expectEvent(new PurchaseRequestValidator(SKU_ENTITY))
                .expectEvent(new PurchaseRequestValidator(SKU_SUBSCRIPTION))
                .expectEvent(new PurchaseRequestValidator(SKU_CONSUMABLE))
                .expectEvent(new PurchaseRequestValidator(SKU_ENTITY))
                .expectEvent(new PurchaseRequestValidator(SKU_SUBSCRIPTION))
                .expectEvent(new PurchaseResponseValidator(billingProviderName1, false))
                .expectEvent(new PurchaseResponseValidator(billingProviderName1, false))
                .expectEvent(new PurchaseResponseValidator(billingProviderName1, false))
                .expectEvent(new PurchaseResponseValidator(billingProviderName2, true))
                .expectEvent(new PurchaseResponseValidator(billingProviderName2, true))
                .expectEvent(new PurchaseResponseValidator(billingProviderName2, true))
                .setStrategy(TestManager.Strategy.UNORDERED_EVENTS)
                .build();

        final Configuration config1 = new Configuration.Builder()
                .addBillingProvider(billingProvider1)
                .setBillingListener(new MyAdapter(testManager))
                .setSubsequentRequestDelay(0)
                .build();

        final Configuration config2 = new Configuration.Builder()
                .addBillingProvider(billingProvider2)
                .setBillingListener(new MyAdapter(testManager))
                .setSubsequentRequestDelay(0)
                .build();

        final String[] skus = new String[]{SKU_CONSUMABLE, SKU_ENTITY, SKU_SUBSCRIPTION};


        for (int i = 0; i < NUM_TESTS; ++i) {
            instrumentation.runOnMainSync(new SetConfig(i % 2 == 0 ? config1 : config2));
            setupLatch = new CountDownLatch(1);
            instrumentation.runOnMainSync(new SetupLib());
            setupLatch.await(MAX_WAIT_TIME, TimeUnit.MILLISECONDS);
            purchaseLatch = new CountDownLatch(1);
            instrumentation.runOnMainSync(new PurchaseSKU(skus[i % skus.length]));
            purchaseLatch.await(MAX_WAIT_TIME, TimeUnit.MILLISECONDS);
            instrumentation.waitForIdleSync();
        }

        assertTrue(testManager.await(MAX_WAIT_TIME * NUM_TESTS));
    }

    private final class MyAdapter extends BillingManagerAdapter {
        public MyAdapter(final TestManager testManager) {
            super(testManager, false);
        }

        @Override
        public void onPurchase(@NonNull final PurchaseResponse purchaseResponse) {
            super.onPurchase(purchaseResponse);
            if (purchaseLatch != null) {
                purchaseLatch.countDown();
            }
        }

        @Override
        public void onSetupResponse(@NonNull final SetupResponse setupResponse) {
            super.onSetupResponse(setupResponse);
            if (setupLatch != null) {
                setupLatch.countDown();
            }
        }
    }

    private final class SetConfig implements Runnable {
        private final Configuration configuration;

        private SetConfig(final Configuration configuration) {
            this.configuration = configuration;
        }

        @Override
        public void run() {
            OPFIab.init(activity.getApplication(), configuration);
        }
    }

    private final class SetupLib implements Runnable {
        @Override
        public void run() {
            OPFIab.setup();
        }
    }

    private final class PurchaseSKU implements Runnable {
        private final String sku;

        private PurchaseSKU(final String sku) {
            this.sku = sku;
        }

        @Override
        public void run() {
            OPFIab.getSimpleHelper().purchase(sku);
        }
    }
}
