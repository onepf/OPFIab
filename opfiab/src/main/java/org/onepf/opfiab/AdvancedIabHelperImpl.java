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

package org.onepf.opfiab;

import android.support.annotation.NonNull;

import org.onepf.opfiab.api.AdvancedIabHelper;
import org.onepf.opfiab.listener.BillingListener;
import org.onepf.opfiab.listener.BillingListenerCompositor;
import org.onepf.opfiab.listener.OnConsumeListener;
import org.onepf.opfiab.listener.OnInventoryListener;
import org.onepf.opfiab.listener.OnPurchaseListener;
import org.onepf.opfiab.listener.OnSetupListener;
import org.onepf.opfiab.listener.OnSkuDetailsListener;
import org.onepf.opfiab.model.event.SetupResponse;
import org.onepf.opfiab.model.event.billing.BillingRequest;
import org.onepf.opfutils.OPFChecks;

class AdvancedIabHelperImpl extends SimpleIabHelperImpl implements AdvancedIabHelper {

    private final BillingRequestScheduler scheduler = BillingRequestScheduler.getInstance();
    private final BillingEventDispatcher dispatcher = BillingEventDispatcher.getInstance();

    private final BillingListenerCompositor listenerCompositor = new BillingListenerCompositor();

    AdvancedIabHelperImpl() {
        super();
    }

    private void deliverLastSetupEvent(@NonNull final OnSetupListener setupListener) {
        final SetupResponse setupResponse = billingBase.getSetupResponse();
        if (setupResponse != null) {
            setupListener.onSetupResponse(setupResponse);
        }
    }

    @Override
    protected void postRequest(@NonNull final BillingRequest billingRequest) {
        if (billingBase.getSetupResponse() == null) {
            // Lazy setup
            OPFIab.setup();
            scheduler.schedule(this, billingRequest);
        } else if (!billingBase.isBusy()) {
            // No need to schedule anything
            super.postRequest(billingRequest);
        } else if (!billingRequest.equals(billingBase.getPendingRequest())) {
            // If request is not already being precessed, schedule it for later
            scheduler.schedule(this, billingRequest);
        }
    }

    @Override
    public void addSetupListener(@NonNull final OnSetupListener setupListener) {
        OPFChecks.checkThread(true);
        listenerCompositor.addSetupListener(setupListener);
        deliverLastSetupEvent(setupListener);
    }

    @Override
    public void addPurchaseListener(@NonNull final OnPurchaseListener purchaseListener) {
        OPFChecks.checkThread(true);
        listenerCompositor.addPurchaseListener(purchaseListener);
    }

    @Override
    public void addInventoryListener(@NonNull final OnInventoryListener inventoryListener) {
        OPFChecks.checkThread(true);
        listenerCompositor.addInventoryListener(inventoryListener);
    }

    @Override
    public void addSkuDetailsListener(@NonNull final OnSkuDetailsListener skuInfoListener) {
        OPFChecks.checkThread(true);
        listenerCompositor.addSkuDetailsListener(skuInfoListener);
    }

    @Override
    public void addConsumeListener(@NonNull final OnConsumeListener consumeListener) {
        OPFChecks.checkThread(true);
        listenerCompositor.addConsumeListener(consumeListener);
    }

    @Override
    public void addBillingListener(@NonNull final BillingListener billingListener) {
        OPFChecks.checkThread(true);
        listenerCompositor.addBillingListener(billingListener);
        deliverLastSetupEvent(billingListener);
    }

    @Override
    public void register() {
        dispatcher.register(listenerCompositor);
    }

    @Override
    public void unregister() {
        dispatcher.unregister(listenerCompositor);
        scheduler.dropQueue(this);
    }
}
