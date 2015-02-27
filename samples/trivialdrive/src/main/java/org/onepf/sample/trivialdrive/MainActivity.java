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

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import org.onepf.opfiab.OPFIab;
import org.onepf.opfiab.SelfManagedIabHelper;
import org.onepf.opfiab.listener.BillingListener;
import org.onepf.opfiab.listener.SimpleBillingListener;
import org.onepf.opfiab.model.event.SetupResponse;
import org.onepf.opfiab.model.event.billing.SkuDetailsResponse;

public class MainActivity extends ActionBarActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String SKU = "org.onepf.sample.trivialdrive.sku_gas";

    @NonNull
    private SelfManagedIabHelper iabHelper;
    @NonNull
    private View button;
    private final BillingListener billingListener = new SimpleBillingListener() {

        @Override
        public void onSetup(@NonNull final SetupResponse setupResponse) {
            super.onSetup(setupResponse);
            button.setEnabled(setupResponse.isSuccessful());
        }

        @Override
        public void onSkuDetails(@NonNull final SkuDetailsResponse skuDetailsResponse) {
            super.onSkuDetails(skuDetailsResponse);
            if (skuDetailsResponse.isSuccessful()) {
                iabHelper.inventory(true);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        iabHelper = OPFIab.getHelper(this);
        setContentView(R.layout.activity_main);

        button = findViewById(R.id.button);
        button.setEnabled(false);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                iabHelper.purchase(SKU);
            }
        });

        iabHelper.addBillingListener(billingListener);

        if (savedInstanceState == null) {
            iabHelper.skuDetails(SKU);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
