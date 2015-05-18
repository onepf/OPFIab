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

import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.test.ActivityInstrumentationTestCase2;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.onepf.opfiab.billing.BillingProvider;
import org.onepf.opfiab.listener.BillingListener;
import org.onepf.opfiab.model.Configuration;
import org.onepf.opfiab.opfiab_uitest.validators.ProviderNameSetupResponseValidator;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

/**
 * @author antonpp
 * @since 15.05.15
 */
@RunWith(AndroidJUnit4.class)
public class ActivityIabHelperTest extends ActivityInstrumentationTestCase2<ActivityHelperActivity> {

    public ActivityIabHelperTest() {
        super(ActivityHelperActivity.class);
    }

    private ActivityHelperActivity activity;
    private TestManager testManager;

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();

        injectInstrumentation(InstrumentationRegistry.getInstrumentation());
        initMocks(this);

        activity = getActivity();
    }

    @Mock BillingProvider billingProvider;

    private void prepareMockProvider(String name) {
        when(billingProvider.isAuthorised()).thenReturn(true);
        when(billingProvider.isAvailable()).thenReturn(true);
        when(billingProvider.getInfo().getName()).thenReturn(name);
    }

    @Test
    public void testSimplePurchase() {
        final Configuration configuration = new Configuration.Builder()
                .addBillingProvider(billingProvider)
                .build();
        final String name = "Absolutely random name";
        prepareMockProvider(name);
        testManager = new TestManager.Builder().addAction(new ProviderNameSetupResponseValidator(name)).build();
        activity.setBillingListener((BillingListener)testManager);
        onView(withId(R.id.button_init)).perform(click());
    }
}
