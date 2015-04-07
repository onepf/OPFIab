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

package org.onepf.sample.trivialdrive;

import android.app.Application;
import android.content.SharedPreferences;

import org.onepf.opfiab.OPFIab;
import org.onepf.opfiab.amazon.AmazonBillingProvider;
import org.onepf.opfiab.billing.BillingProvider;
import org.onepf.opfiab.google.GoogleBillingProvider;
import org.onepf.opfiab.google.GoogleMapSkuResolver;
import org.onepf.opfiab.listener.DefaultBillingListener;
import org.onepf.opfiab.model.Configuration;
import org.onepf.opfiab.model.billing.SkuType;
import org.onepf.opfiab.sku.MapSkuResolver;
import org.onepf.opfutils.OPFLog;

import static org.onepf.sample.trivialdrive.TrivialUtils.AMAZON_SKU_GAS;
import static org.onepf.sample.trivialdrive.TrivialUtils.GOOGLE_SKU_GAS;
import static org.onepf.sample.trivialdrive.TrivialUtils.SKU_GAS;


public class TrivialApplication extends Application {

    private static final String KEY_FIRST_LAUNCH = "first_launch";
    private static final int INITIAL_FUEL = 2;


    @Override
    public void onCreate() {
        super.onCreate();

        checkFirstLaunch();
        initOPFIab();
    }

    private void checkFirstLaunch() {
        final SharedPreferences preferences = TrivialUtils.getPreferences(this);
        if (!preferences.getBoolean(KEY_FIRST_LAUNCH, true)) {
            return;
        }

        final SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(KEY_FIRST_LAUNCH, false);
        editor.putInt(TrivialUtils.KEY_GAS, INITIAL_FUEL);
        editor.apply();
    }

    private void initOPFIab() {
        OPFLog.setEnabled(BuildConfig.DEBUG, true);
        final Configuration configuration = new Configuration.Builder()
                .addBillingProvider(newGoogleBillingProvider())
                .addBillingProvider(newAmazonBillingProvider())
                .setBillingListener(new DefaultBillingListener())
                .setAutoRecover(true)
                .setSkipUnauthorised(false)
                .build();
        OPFIab.init(this, configuration);
        OPFIab.setup();
    }

    private BillingProvider newGoogleBillingProvider() {
        final GoogleMapSkuResolver skuResolver = new GoogleMapSkuResolver();
        skuResolver.add(SKU_GAS, GOOGLE_SKU_GAS, SkuType.CONSUMABLE);

        return new GoogleBillingProvider.Builder(this)
                //                .setPurchaseVerifier(new SimpleGooglePurchaseVerifier(GOOGLE_PLAY_KEY))
                .setSkuResolver(skuResolver)
                .build();
    }

    private BillingProvider newAmazonBillingProvider() {
        final MapSkuResolver skuResolver = new MapSkuResolver();
        skuResolver.add(SKU_GAS, AMAZON_SKU_GAS);

        return new AmazonBillingProvider.Builder(this)
                .setSkuResolver(skuResolver)
                .build();
    }
}
