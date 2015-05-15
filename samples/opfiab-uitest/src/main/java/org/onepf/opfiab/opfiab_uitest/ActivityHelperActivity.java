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
import android.support.annotation.NonNull;
import android.view.View;

import org.onepf.opfiab.OPFIab;
import org.onepf.opfiab.api.ActivityIabHelper;
import org.onepf.opfiab.billing.BillingProvider;
import org.onepf.opfiab.google.GoogleMapSkuResolver;
import org.onepf.opfiab.google.GoogleSkuResolver;
import org.onepf.opfiab.listener.BillingListener;
import org.onepf.opfiab.listener.DefaultBillingListener;
import org.onepf.opfiab.model.Configuration;
import org.onepf.opfiab.opfiab_uitest.mock.MockFailBillingProvider;
import org.onepf.opfiab.opfiab_uitest.mock.MockOkBillingProvider;
import org.onepf.opfiab.sku.SkuResolver;
import org.onepf.opfutils.OPFLog;

public class ActivityHelperActivity extends Activity implements View.OnClickListener {

    private static final String TAG = ActivityHelperActivity.class.getSimpleName();

    private static final String SKU_CONSUMABLE = "org.onepf.opfiab.consumable";
    private static final String SKU_NONCONSUMABLE = "org.onepf.opfiab.nonconsumable";
    private static final String SKU_SUBSCRIPTION = "org.onepf.opfiab.subscription";

    private BillingListener billingListener;

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
        OPFLog.setEnabled(true, true);

        findViewById(R.id.button_init_ok).setOnClickListener(this);
        findViewById(R.id.button_init_fail).setOnClickListener(this);
        findViewById(R.id.button_setup).setOnClickListener(this);
        findViewById(R.id.button_buy_consumable).setOnClickListener(this);
        findViewById(R.id.button_buy_nonconsumable).setOnClickListener(this);
        findViewById(R.id.button_buy_subscription).setOnClickListener(this);

        initHelper(true);
        setupHelper();
        iabHelper = OPFIab.getActivityHelper(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button_init_ok:
                initHelper(true);
                break;
            case R.id.button_init_fail:
                initHelper(false);
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

    private void initHelper(boolean needOkBillingProvider) {
        final BillingProvider billingProvider;
        if (needOkBillingProvider) {
            billingProvider = new MockOkBillingProvider();
        } else {
            billingProvider = new MockFailBillingProvider();
        }

        final GoogleSkuResolver skuResolver = new GoogleMapSkuResolver();

        OPFIab.init(getApplication(),
                new Configuration.Builder()
                        .addBillingProvider(billingProvider)
                        .setBillingListener(billingListener = new DefaultBillingListener())
                        .build()
        );
    }

    private void setupHelper() {
        OPFIab.setup();
    }

    private void buyConsumable() {
        iabHelper.purchase(SKU_CONSUMABLE);
    }

    private void buyNonconsumable() {
        iabHelper.purchase(SKU_NONCONSUMABLE);
    }

    private void buySubscription() {
        iabHelper.purchase(SKU_SUBSCRIPTION);

    }
}
