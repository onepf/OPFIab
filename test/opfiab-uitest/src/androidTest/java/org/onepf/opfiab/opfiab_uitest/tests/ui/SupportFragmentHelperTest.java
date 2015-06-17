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

import android.app.Instrumentation;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;
import android.support.test.uiautomator.UiDevice;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.onepf.opfiab.opfiab_uitest.EmptyFragmentActivity;
import org.onepf.opfiab.opfiab_uitest.util.Constants;

/**
 * @author antonpp
 * @since 04.06.15
 */
public class SupportFragmentHelperTest {

    @Rule
    public final ActivityTestRule<EmptyFragmentActivity> testRule = new ActivityTestRule<>(
            EmptyFragmentActivity.class);

    private Instrumentation instrumentation;
    private UiDevice uiDevice;
    private EmptyFragmentActivity activity;

    @Before
    public void setUp() {
        instrumentation = InstrumentationRegistry.getInstrumentation();
        activity = testRule.getActivity();
        uiDevice = UiDevice.getInstance(instrumentation);
    }

    @Test
    public void testRegisterUnregisterHomeButton() throws InterruptedException {
        UnifiedFragmentHelperTest.testRegisterUnregisterHomeButton(instrumentation, activity,
                                                                   uiDevice);
    }


    @Test
    public void testRegisterUnregisterFragmentReplace() throws InterruptedException {
        UnifiedFragmentHelperTest.testRegisterUnregisterFragmentReplace(instrumentation, true,
                                                                        uiDevice);
    }

    @After
    public void tearDown() throws InterruptedException {
        Thread.sleep(Constants.WAIT_TEST_MANAGER);
    }
}
