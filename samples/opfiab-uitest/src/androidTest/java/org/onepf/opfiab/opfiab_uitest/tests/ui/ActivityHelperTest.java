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

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.test.ActivityInstrumentationTestCase2;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.onepf.opfiab.listener.BillingListener;
import org.onepf.opfiab.listener.SimpleBillingListener;
import org.onepf.opfiab.model.Configuration;
import org.onepf.opfiab.model.event.SetupResponse;
import org.onepf.opfiab.model.event.billing.PurchaseResponse;
import org.onepf.opfiab.opfiab_uitest.ActivityHelperActivity;
import org.onepf.opfiab.opfiab_uitest.R;
import org.onepf.opfiab.opfiab_uitest.mock.MockBillingProvider;
import org.onepf.opfiab.opfiab_uitest.mock.MockFailBillingProvider;
import org.onepf.opfiab.opfiab_uitest.mock.MockOkBillingProvider;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

/**
 * @author antonpp
 * @since 14.05.15
 */
@RunWith(AndroidJUnit4.class)
public class ActivityHelperTest extends ActivityInstrumentationTestCase2<ActivityHelperActivity> {

    private static final String TAG = ActivityHelperTest.class.getSimpleName();
    private static final long MAX_WAIT_TIME = MockBillingProvider.SLEEP_TIME * 4l;
    private static final int TESTS_COUNT = 20;
    private static final Random RND = new Random();
    private static final long MAX_SUBSEQUENT_DELAY = 300L;

    @Nullable
    private ActivityHelperActivity activity;

    public ActivityHelperTest() {
        super(ActivityHelperActivity.class);
    }

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();

        injectInstrumentation(InstrumentationRegistry.getInstrumentation());

        activity = getActivity();
    }

    @After
    @Override
    public void tearDown() throws Exception {
        super.tearDown();
        activity = null;
    }

    @Test
    public void testSuccessfulPurchase() {
        final CountDownLatch purchaseLatch = new CountDownLatch(1);
        prepareBillingListener(purchaseLatch, null);

        initSetupBuy(R.id.button_init_ok);

        checkExpectedCalls(purchaseLatch, MAX_WAIT_TIME,
                           "Failed to receive a correct purchase callback");
    }

    private void prepareBillingListener(CountDownLatch successLatch, CountDownLatch failLatch) {
        final CountDownLatch threadSyncLatch = new CountDownLatch(1);
        final BillingListener listener = new TestBillingListener(successLatch, failLatch);

        assert activity != null;

        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.setBillingListener(listener);
                threadSyncLatch.countDown();
            }
        });

        awaitLatch(threadSyncLatch);
    }

    private void initSetupBuy(int initBtnId) {
        onView(withId(initBtnId)).perform(click());
        onView(withId(R.id.button_setup)).perform(click());
        onView(withId(R.id.button_buy_consumable)).perform(click());
    }

    private void checkExpectedCalls(CountDownLatch latch, long timeout, String msg) {
        awaitLatch(latch, timeout);
        if (latch.getCount() != 0) {
            Assert.fail(msg);
        }
    }

    private void awaitLatch(CountDownLatch latch) {
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void awaitLatch(CountDownLatch latch, long ms) {
        try {
            latch.await(ms, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testFailedPurchase() {
        final CountDownLatch purchaseLatch = new CountDownLatch(1);
        prepareBillingListener(null, purchaseLatch);

        initSetupBuy(R.id.button_init_fail);

        checkExpectedCalls(purchaseLatch, MAX_WAIT_TIME,
                           "Failed to receive a correct purchase callback");
    }

    @Test
    public void testMultipleSuccessfulPurchases() {
        final CountDownLatch purchaseLatch = new CountDownLatch(TESTS_COUNT);
        prepareBillingListener(purchaseLatch, null);

        onView(withId(R.id.button_init_ok)).perform(click());
        onView(withId(R.id.button_setup)).perform(click());

        for (int i = 0; i < TESTS_COUNT; ++i) {
            onView(withId(R.id.button_buy_consumable)).perform(click());
        }

        checkExpectedCalls(purchaseLatch, MAX_WAIT_TIME * TESTS_COUNT, "Failed to receive a correct number of purchase callbacks");
    }

    @Test
    public void testMultiInitSetup() {
        final int expectedOk = RND.nextInt(TESTS_COUNT);
        final Collection<Boolean> tests = new ArrayList<>(TESTS_COUNT);
        for (int i = 0; i < expectedOk; ++i) {
            tests.add(true);
        }
        for (int i = expectedOk; i < TESTS_COUNT; ++i) {
            tests.add(false);
        }
        final CountDownLatch okLatch = new CountDownLatch(expectedOk);
        final CountDownLatch failLatch = new CountDownLatch(TESTS_COUNT - expectedOk);
        final CountDownLatch setupLatch = new CountDownLatch(expectedOk);


        prepareBillingListener(okLatch, failLatch);
        prepareSetupListener(setupLatch);

        Assert.assertNotNull(activity);

        for (boolean isOk : tests) {
            activity.setCustomConfiguration(getRandomConfiguration(isOk));
            initSetupBuy(R.id.button_init);
        }

        String msg = String.format("Failed to receive all successful purchases callbacks (%d of %d)"
                , expectedOk - okLatch.getCount(), expectedOk);
        checkExpectedCalls(okLatch, MAX_WAIT_TIME * TESTS_COUNT, msg);
        msg = String.format("Failed to receive all failed purchases callbacks (%d of %d)"
                , TESTS_COUNT - expectedOk - failLatch.getCount(), TESTS_COUNT - expectedOk);
        checkExpectedCalls(failLatch, MAX_WAIT_TIME, msg);
        msg = String.format("Not all registrations were successful (%d of %d[%d])"
                , TESTS_COUNT - setupLatch.getCount(), TESTS_COUNT, expectedOk);
        checkExpectedCalls(setupLatch, MAX_WAIT_TIME, msg);
    }

    private void prepareSetupListener(final CountDownLatch successLatch) {
        final CountDownLatch threadSyncLatch = new CountDownLatch(1);
        final BillingListener listener = new SimpleBillingListener() {
            @Override
            public void onSetupResponse(@NonNull SetupResponse setupResponse) {
                super.onSetupResponse(setupResponse);
                if (setupResponse.isSuccessful()) {
                    successLatch.countDown();
                }
            }
        };

        assert activity != null;

        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.setBillingListener(listener);
                threadSyncLatch.countDown();
            }
        });

        awaitLatch(threadSyncLatch);
    }

    private Configuration getRandomConfiguration(boolean isOk) {
        return new Configuration.Builder()
                .addBillingProvider(isOk ? new MockOkBillingProvider() : new MockFailBillingProvider())
                .setAutoRecover(RND.nextBoolean())
                .setSkipUnauthorised(RND.nextBoolean())
                .setSubsequentRequestDelay(RND.nextLong() % MAX_SUBSEQUENT_DELAY)
                .build();
    }

    private static final class TestBillingListener extends SimpleBillingListener {

        private final CountDownLatch successLatch;
        private final CountDownLatch failLatch;

        public TestBillingListener(CountDownLatch successLatch, CountDownLatch failLatch) {
            this.successLatch = successLatch;
            this.failLatch = failLatch;
        }

        @Override
        public void onPurchase(@NonNull PurchaseResponse purchaseResponse) {
            super.onPurchase(purchaseResponse);
            if (purchaseResponse.isSuccessful()) {
                if (successLatch != null) {
                    successLatch.countDown();
                }
            } else {
                if (failLatch != null) {
                    failLatch.countDown();
                }
            }
        }
    }
}
