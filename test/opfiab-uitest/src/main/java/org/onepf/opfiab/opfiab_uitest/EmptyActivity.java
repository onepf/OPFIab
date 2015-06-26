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

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import org.onepf.opfiab.OPFIab;
import org.onepf.opfiab.model.event.android.ActivityResult;

/**
 * @author antonpp
 * @since 26.05.15
 */
@SuppressWarnings("AssignmentToStaticFieldFromInstanceMethod")
public class EmptyActivity extends Activity {

    private static EmptyActivity lastInstance;

    public static EmptyActivity getLastInstance() {
        return lastInstance;
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_empty);
        lastInstance = this;
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode,
                                    final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        OPFIab.post(new ActivityResult(this, requestCode, resultCode, data));
    }
}
