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

package org.onepf.sample.trivialdrive.ui.activity;

import android.content.Intent;
import android.os.Bundle;

import org.onepf.opfiab.OPFIab;
import org.onepf.opfiab.api.ActivityIabHelper;
import org.onepf.sample.trivialdrive.R;
import org.onepf.sample.trivialdrive.ui.view.TrivialView;

public class ActivityHelperActivity extends TrivialActivity {

    private ActivityIabHelper iabHelper;

    private TrivialView trivialView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        iabHelper = OPFIab.getActivityHelper(this);
        setContentView(R.layout.activity_trivial);

        trivialView = (TrivialView) findViewById(R.id.trivial_drive);
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode,
                                    final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        iabHelper.onActivityResult(this, requestCode, resultCode, data);
    }
}
