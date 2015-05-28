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

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.onepf.opfiab.OPFIab;
import org.onepf.opfiab.api.AdvancedIabHelper;
import org.onepf.opfiab.billing.BillingProvider;
import org.onepf.opfiab.model.BillingProviderInfo;
import org.onepf.opfiab.model.Configuration;
import org.onepf.opfiab.opfiab_uitest.util.MockBillingProviderBuilder;
import org.onepf.opfiab.opfiab_uitest.validators.SetupResponseValidator;
import org.onepf.opfiab.opfiab_uitest.validators.SetupStartedEventValidator;

import static junit.framework.Assert.assertTrue;

/**
 * @author antonpp
 * @since 28.05.15
 */
public class AdvancedHelperTest {

    private static final long MAX_WAIT_TIME = 1000L;

    @Rule
    public ActivityTestRule<EmptyActivity> testRule = new ActivityTestRule<>(EmptyActivity.class);

    private AdvancedIabHelper iabHelper;
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

    @Test
    public void testSetup() throws InterruptedException {
        final String name = "Absolutely random name";
        final BillingProvider billingProvider = prepareMockProvider(name);

        final TestManager testManager = new TestManager.Builder()
                .expectEvent(new SetupStartedEventValidator())
                .expectEvent(new SetupResponseValidator(name))
                .build();

        instrumentation.runOnMainSync(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                final Configuration configuration = new Configuration.Builder()
                        .addBillingProvider(billingProvider)
                        .setBillingListener(testManager)
                        .build();
                OPFIab.init(activity.getApplication(), configuration);
                iabHelper = OPFIab.getAdvancedHelper();
                OPFIab.setup();
            }
        });

        assertTrue(testManager.startTest(MAX_WAIT_TIME));
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
