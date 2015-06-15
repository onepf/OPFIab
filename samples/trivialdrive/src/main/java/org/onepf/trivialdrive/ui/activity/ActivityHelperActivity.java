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

package org.onepf.trivialdrive.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;

import org.onepf.opfiab.OPFIab;
import org.onepf.opfiab.api.ActivityIabHelper;
import org.onepf.opfiab.listener.OnInventoryListener;
import org.onepf.opfiab.listener.OnPurchaseListener;
import org.onepf.opfiab.listener.OnSetupListener;
import org.onepf.opfiab.listener.OnSkuDetailsListener;
import org.onepf.opfiab.model.event.SetupResponse;
import org.onepf.opfiab.model.event.SetupStartedEvent;
import org.onepf.opfiab.model.event.billing.InventoryResponse;
import org.onepf.opfiab.model.event.billing.PurchaseResponse;
import org.onepf.opfiab.model.event.billing.SkuDetailsResponse;
import org.onepf.trivialdrive.R;
import org.onepf.trivialdrive.ui.view.TrivialView;

public class ActivityHelperActivity extends TrivialActivity
        implements OnSetupListener, OnPurchaseListener, OnInventoryListener, OnSkuDetailsListener {

    private ActivityIabHelper iabHelper;
    private TrivialView trivialView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        iabHelper = OPFIab.getActivityHelper(this);
        setContentView(R.layout.include_trivial);
        trivialView = (TrivialView) findViewById(R.id.trivial);
        trivialView.setIabHelper(iabHelper);

        iabHelper.addPurchaseListener(this);
        iabHelper.addInventoryListener(this);
        iabHelper.addSkuDetailsListener(this);
        iabHelper.addSetupListener(this, false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Some billing operations may involve another activities
        // It's a good idea to check for updates here
        trivialView.update();
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode,
                                    final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        iabHelper.onActivityResult(this, requestCode, resultCode, data);
    }

    @Override
    public void onSetupStarted(@NonNull final SetupStartedEvent setupStartedEvent) {
        // might be a good place to show progress
        trivialView.update();
    }

    @Override
    public void onSetupResponse(@NonNull final SetupResponse setupResponse) {
        // good place to hide progress
    }

    @Override
    public void onPurchase(@NonNull final PurchaseResponse purchaseResponse) {
        trivialView.updatePremium();
        trivialView.updateSubscription();
    }

    @Override
    public void onInventory(@NonNull final InventoryResponse inventoryResponse) {
        trivialView.updatePremium();
        trivialView.updateSubscription();
    }

    @Override
    public void onSkuDetails(@NonNull final SkuDetailsResponse skuDetailsResponse) {
        trivialView.updateSkuDetails();
    }
}
