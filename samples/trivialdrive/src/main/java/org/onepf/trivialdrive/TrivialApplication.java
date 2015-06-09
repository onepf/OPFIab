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

import android.app.Application;
import android.content.Context;

import com.squareup.leakcanary.LeakCanary;
import com.squareup.leakcanary.RefWatcher;

import org.onepf.opfiab.OPFIab;
import org.onepf.opfutils.OPFLog;


public class TrivialApplication extends Application {

    public static RefWatcher getRefWatcher(final Context context) {
        final Context applicationContext = context.getApplicationContext();
        final TrivialApplication application = (TrivialApplication) applicationContext;
        return application.refWatcher;
    }


    private RefWatcher refWatcher;

    @Override
    public void onCreate() {
        super.onCreate();
        TrivialData.init(this);
        TrivialBilling.init(this);

        OPFLog.setEnabled(true, true);
        OPFIab.init(this, TrivialBilling.getRelevantConfiguration(this));

        refWatcher = LeakCanary.install(this);
    }

}
