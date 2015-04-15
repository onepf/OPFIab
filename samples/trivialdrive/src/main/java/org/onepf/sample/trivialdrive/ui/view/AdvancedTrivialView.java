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

package org.onepf.sample.trivialdrive.ui.view;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.AttributeSet;

import org.onepf.opfiab.OPFIab;
import org.onepf.opfiab.api.AdvancedIabHelper;
import org.onepf.opfiab.listener.OnInventoryListener;
import org.onepf.opfiab.listener.OnSetupListener;
import org.onepf.opfiab.listener.OnSkuDetailsListener;
import org.onepf.opfiab.model.event.SetupResponse;
import org.onepf.opfiab.model.event.SetupStartedEvent;
import org.onepf.opfiab.model.event.billing.InventoryResponse;
import org.onepf.opfiab.model.event.billing.SkuDetailsResponse;
import org.onepf.sample.trivialdrive.TrivialBilling;

public class AdvancedTrivialView extends TrivialView
        implements OnSetupListener, OnInventoryListener, OnSkuDetailsListener {

    private AdvancedIabHelper iabHelper;

    public AdvancedTrivialView(final Context context) {
        super(context);
    }

    public AdvancedTrivialView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
    }

    public AdvancedTrivialView(final Context context, final AttributeSet attrs,
                               final int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public AdvancedTrivialView(final Context context, final AttributeSet attrs,
                               final int defStyleAttr, final int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void init() {
        super.init();
        iabHelper = OPFIab.getAdvancedHelper();
        setIabHelper(iabHelper);
        iabHelper.addInventoryListener(this);
        iabHelper.addSkuDetailsListener(this);
        iabHelper.addSetupListener(this);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        iabHelper.register();
    }

    @Override
    protected void onDetachedFromWindow() {
        iabHelper.unregister();
        super.onDetachedFromWindow();
    }

    @Override
    public void onSetupStarted(@NonNull final SetupStartedEvent setupStartedEvent) {
        setEnabled(false);
    }

    @Override
    public void onSetupResponse(@NonNull final SetupResponse setupResponse) {
        final boolean successful = setupResponse.isSuccessful();
        setEnabled(successful);
        if (successful) {
            iabHelper.inventory(true);
        }
    }

    @Override
    public void onInventory(@NonNull final InventoryResponse inventoryResponse) {
        setHasPremium(TrivialBilling.hasPremium(inventoryResponse));
        setHasSubscription(TrivialBilling.hasValidSubscription(inventoryResponse));
        if (inventoryResponse.isSuccessful()) {
            requestSkuDetails();
        }
    }

    @Override
    public void onSkuDetails(@NonNull final SkuDetailsResponse skuDetailsResponse) {
        setSkuDetailsResponse(skuDetailsResponse);
    }
}
