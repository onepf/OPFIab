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
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import org.onepf.opfiab.OPFIab;
import org.onepf.opfiab.SelfManagedIabHelper;
import org.onepf.opfiab.listener.OnPurchaseListener;
import org.onepf.opfiab.listener.OnSetupListener;
import org.onepf.opfiab.model.event.SetupEvent;
import org.onepf.opfiab.model.event.response.PurchaseResponse;

public class MainActivity extends ActionBarActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    @NonNull
    private SelfManagedIabHelper iabHelper;

    private final OnSetupListener setupListener = new OnSetupListener() {
        @Override
        public void onSetup(@NonNull final SetupEvent setupEvent) {
            Log.d(TAG, "" + setupEvent);
        }
    };

    private final OnPurchaseListener purchaseListener = new OnPurchaseListener() {
        @Override
        public void onPurchase(@NonNull final PurchaseResponse purchaseResponse) {
            Log.d(TAG, "" + purchaseResponse);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        iabHelper = OPFIab.getHelper(this);
        iabHelper.addSetupListener(setupListener);
        iabHelper.addPurchaseListener(purchaseListener);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
//            iabHelper.purchase(new ConsumableDetails("org.onepf.sample.trivialdrive.sku_gas"));
            iabHelper.skuDetails("org.onepf.sample.trivialdrive.sku_gas");
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
