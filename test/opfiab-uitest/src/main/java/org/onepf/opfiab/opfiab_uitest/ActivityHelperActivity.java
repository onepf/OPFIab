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
import android.view.View;

import org.onepf.opfiab.OPFIab;
import org.onepf.opfiab.api.ActivityIabHelper;
import org.onepf.opfiab.billing.BillingProvider;
import org.onepf.opfiab.listener.BillingListener;
import org.onepf.opfiab.listener.DefaultBillingListener;
import org.onepf.opfiab.model.Configuration;
import org.onepf.opfiab.opfiab_uitest.mock.MockFailBillingProvider;
import org.onepf.opfiab.opfiab_uitest.mock.MockOkBillingProvider;
import org.onepf.opfutils.OPFLog;

public class ActivityHelperActivity extends Activity implements View.OnClickListener {

    private static final String SKU_CONSUMABLE = "org.onepf.opfiab.consumable";
    private static final String SKU_ENTITY = "org.onepf.opfiab.entity";
    private static final String SKU_SUBSCRIPTION = "org.onepf.opfiab.subscription";

    private ActivityIabHelper iabHelper;
    private Configuration customConfiguration;

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

        findViewById(R.id.button_init).setOnClickListener(this);
        findViewById(R.id.button_init_ok).setOnClickListener(this);
        findViewById(R.id.button_init_fail).setOnClickListener(this);
        findViewById(R.id.button_setup).setOnClickListener(this);
        findViewById(R.id.button_buy_consumable).setOnClickListener(this);
        findViewById(R.id.button_buy_nonconsumable).setOnClickListener(this);
        findViewById(R.id.button_buy_subscription).setOnClickListener(this);

        initHelper(true);
        iabHelper = OPFIab.getActivityHelper(this);
    }

    private void initHelper(boolean needOkBillingProvider) {
        final BillingProvider billingProvider;
        if (needOkBillingProvider) {
            billingProvider = new MockOkBillingProvider();
        } else {
            billingProvider = new MockFailBillingProvider();
        }

        OPFIab.init(getApplication(),
                new Configuration.Builder()
                        .addBillingProvider(billingProvider)
                        .setBillingListener(new DefaultBillingListener())
                        .build()
        );
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button_init:
                initHelper();
                break;
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

    private void initHelper() {
        if (customConfiguration == null) {
            initHelper(false);
        } else {
            OPFIab.init(getApplication(), customConfiguration);
        }
    }

    private void setupHelper() {
        OPFIab.setup();
    }

    private void buyConsumable() {
        iabHelper.purchase(SKU_CONSUMABLE);
    }

    private void buyNonconsumable() {
        iabHelper.purchase(SKU_ENTITY);
    }

    private void buySubscription() {
        iabHelper.purchase(SKU_SUBSCRIPTION);
    }

    public void setCustomConfiguration(Configuration customConfiguration) {
        this.customConfiguration = customConfiguration;
    }

    public void setBillingListener(BillingListener listener) {
        iabHelper.addBillingListener(listener);
    }

    public ActivityIabHelper getIabHelper() {
        return iabHelper;
    }
}
