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

package org.onepf.sample.trivialdrive.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;

import org.onepf.opfiab.OPFIab;
import org.onepf.opfiab.api.ActivityIabHelper;
import org.onepf.opfiab.listener.OnInventoryListener;
import org.onepf.opfiab.listener.OnSetupListener;
import org.onepf.opfiab.model.event.SetupResponse;
import org.onepf.opfiab.model.event.SetupStartedEvent;
import org.onepf.opfiab.model.event.billing.InventoryResponse;
import org.onepf.sample.trivialdrive.R;
import org.onepf.sample.trivialdrive.TrivialBilling;
import org.onepf.sample.trivialdrive.ui.view.TrivialView;

import static org.onepf.sample.trivialdrive.TrivialBilling.SKU_GAS;
import static org.onepf.sample.trivialdrive.TrivialBilling.SKU_PREMIUM;
import static org.onepf.sample.trivialdrive.TrivialBilling.SKU_SUBSCRIPTION;

public class ActivityHelperActivity extends TrivialActivity
        implements OnSetupListener, OnInventoryListener {

    private ActivityIabHelper iabHelper;
    private TrivialView trivialView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        iabHelper = OPFIab.getActivityHelper(this);
        setContentView(R.layout.include_trivial);
        trivialView = (TrivialView) findViewById(R.id.trivial_drive);
        trivialView.setIabHelper(iabHelper);
        if (savedInstanceState == null) {
            iabHelper.inventory(true);
        }
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode,
                                    final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        iabHelper.onActivityResult(this, requestCode, resultCode, data);
    }

    @Override
    public void onSetupStarted(@NonNull final SetupStartedEvent setupStartedEvent) {
        trivialView.setEnabled(false);
    }

    @Override
    public void onSetupResponse(@NonNull final SetupResponse setupResponse) {
        trivialView.setEnabled(setupResponse.isSuccessful());
    }

    @Override
    public void onInventory(@NonNull final InventoryResponse inventoryResponse) {
        trivialView.setHasPremium(TrivialBilling.hasPremium(inventoryResponse));
        trivialView.setHasSubscription(TrivialBilling.hasValidSubscription(inventoryResponse));
    }
}
