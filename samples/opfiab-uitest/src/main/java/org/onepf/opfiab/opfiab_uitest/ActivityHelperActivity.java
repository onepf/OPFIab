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
import android.util.Log;
import android.view.View;

import org.onepf.opfiab.api.ActivityIabHelper;

public class ActivityHelperActivity extends Activity implements View.OnClickListener {

    private static final String TAG = ActivityHelperActivity.class.getSimpleName();

    private ActivityIabHelper iabHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setup();
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode,
                                    final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        iabHelper.onActivityResult(this, requestCode, resultCode, data);
    }

    private void setup() {
        findViewById(R.id.button_init).setOnClickListener(this);
        findViewById(R.id.button_setup).setOnClickListener(this);
        findViewById(R.id.button_buy_consumable).setOnClickListener(this);
        findViewById(R.id.button_buy_nonconsumable).setOnClickListener(this);
        findViewById(R.id.button_buy_subscription).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button_init:
                initHelper();
                break;
            case R.id.button_setup:
                setupHelper();
                break;
            case R.id.button_buy_consumable:
                buyConsumable();
                break;
            case R.id.button_buy_nonconsumable:
                buyNonconsumable();
                break;
            case R.id.button_buy_subscription:
                buySubscription();
                break;
        }
    }

    private void initHelper() {}

    private void setupHelper() {}

    private void buyConsumable() {}

    private void buyNonconsumable() {}

    private void buySubscription() {}
}
