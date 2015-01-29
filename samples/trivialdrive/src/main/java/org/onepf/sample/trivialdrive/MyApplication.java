/*
 * Copyright 2012-2014 One Platform Foundation
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

package org.onepf.sample.trivialdrive;

import android.app.Application;

import org.onepf.opfiab.BillingProvider;
import org.onepf.opfiab.OPFIab;
import org.onepf.opfiab.amazon.AmazonBillingProvider;
import org.onepf.opfiab.model.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class MyApplication extends Application {

    private static final Logger LOGGER = LoggerFactory.getLogger(MyApplication.class);


    @Override
    public void onCreate() {
        super.onCreate();

        final Configuration configuration = new Configuration.Builder()
                .addBillingProvider(createAmazonBillingProvider())
                .setBillingListener(new TrivialBillingListener())
                .build();

        OPFIab.init(this, configuration);
    }

    private BillingProvider createAmazonBillingProvider() {
        return new AmazonBillingProvider.Builder(this)
                .build();
    }
}
