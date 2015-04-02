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

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBarActivity;
import android.view.View;

import org.onepf.opfiab.OPFIab;
import org.onepf.opfiab.api.ActivityIabHelper;
import org.onepf.opfiab.listener.BillingListener;
import org.onepf.opfiab.listener.SimpleBillingListener;
import org.onepf.opfiab.model.event.SetupStartedEvent;
import org.onepf.opfiab.model.event.SetupResponse;
import org.onepf.opfiab.model.event.billing.SkuDetailsResponse;

import static org.onepf.sample.trivialdrive.TrivialConstants.SKU_GAS;

public class TrivialActivity extends ActionBarActivity {

    @NonNull
    private ActivityIabHelper iabHelper;
    @NonNull
    private View button;
    private final BillingListener billingListener = new SimpleBillingListener() {

        @Override
        public void onSetupStarted(@NonNull final SetupStartedEvent setupStartedEvent) {
            super.onSetupStarted(setupStartedEvent);
            button.setEnabled(false);
        }

        @Override
        public void onSetupResponse(@NonNull final SetupResponse setupResponse) {
            super.onSetupResponse(setupResponse);
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
        iabHelper = OPFIab.getActivityHelper(this);
        setContentView(R.layout.trivial_layout);

        button = findViewById(R.id.btn_buy);
        button.setEnabled(false);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                iabHelper.purchase(SKU_GAS);
                //                OPFIab.getHelper().purchase(SKU_GAS);
                //                OPFIab.getAdvancedHelper().purchase(SKU_GAS);
                //                OPFIab.getActivityHelper(TrivialActivity.this);
            }
        });

        iabHelper.addBillingListener(billingListener);

        if (savedInstanceState == null) {
            iabHelper.skuDetails(SKU_GAS);
        }
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode,
                                    final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        iabHelper.onActivityResult(this, requestCode, resultCode, data);
    }
}
