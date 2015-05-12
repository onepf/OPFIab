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

package org.onepf.trivialdrive;

import android.app.Activity;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.test.ActivityInstrumentationTestCase2;
import android.util.Log;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.onepf.trivialdrive.ui.activity.LauncherActivity;

/**
 * @author antonpp
 * @since 08.05.15
 */
@RunWith(AndroidJUnit4.class)
public class LaunchTest extends ActivityInstrumentationTestCase2<LauncherActivity> {

    private static final String TAG = LaunchTest.class.getSimpleName();

    private Activity activity;

    public LaunchTest() {
        super(LauncherActivity.class);
    }

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();

        injectInstrumentation(InstrumentationRegistry.getInstrumentation());

        activity = getActivity();
    }

    @Test
    public void testCorrectSetup() throws Exception {
        Log.v(TAG, "LauncherActivity has started");
    }

    @After
    @Override
    public void tearDown() throws Exception {
        super.tearDown();
        activity = null;
    }
}
