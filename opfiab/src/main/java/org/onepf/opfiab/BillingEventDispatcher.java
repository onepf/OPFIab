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
import android.support.annotation.Nullable;

import org.onepf.opfiab.listener.BillingListener;
import org.onepf.opfiab.listener.BillingListenerCompositor;
import org.onepf.opfiab.model.event.SetupResponse;
import org.onepf.opfiab.model.event.SetupStartedEvent;
import org.onepf.opfiab.model.event.billing.BillingRequest;
import org.onepf.opfiab.model.event.billing.BillingResponse;
import org.onepf.opfiab.model.event.billing.ConsumeResponse;
import org.onepf.opfiab.model.event.billing.InventoryResponse;
import org.onepf.opfiab.model.event.billing.PurchaseResponse;
import org.onepf.opfiab.model.event.billing.SkuDetailsResponse;
import org.onepf.opfutils.OPFChecks;
import org.onepf.opfutils.OPFLog;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * This class helps to deliver all billing events to appropriate listeners.
 * <br>
 * It's intended to exist as singleton and allow to add and remove corresponding listeners by
 * {@link #register(BillingListener)} and {@link #unregister(BillingListener)} methods.
 */
final class BillingEventDispatcher extends BillingListenerCompositor {

    @Nullable
    private static BillingEventDispatcher instance;

    @SuppressWarnings({"PMD.NonThreadSafeSingleton"})
    static BillingEventDispatcher getInstance() {
        OPFChecks.checkThread(true);
        if (instance == null) {
            instance = new BillingEventDispatcher();
        }
        return instance;
    }


    private BillingEventDispatcher() {
        super();
    }

    /**
     * Registers listener to receive all billing events.
     *
     * @param billingListener Listener object to register.
     */
    void register(@NonNull final BillingListener billingListener) {
        addBillingListener(billingListener);
    }

    /**
     * Unregisters listener from receiving any billing events.
     *
     * @param billingListener Listener object to unregister.
     */
    void unregister(@NonNull final BillingListener billingListener) {
        removeBillingListener(billingListener);
    }

    protected void removeBillingListener(@NonNull final BillingListener billingListener) {
        billingListeners.remove(billingListener);
        setupListeners.remove(billingListener);
        purchaseListeners.remove(billingListener);
        consumeListeners.remove(billingListener);
        inventoryListeners.remove(billingListener);
        skuDetailsListeners.remove(billingListener);
    }

    public void onEventMainThread(@NonNull final SetupStartedEvent setupStartedEvent) {
        onSetupStarted(setupStartedEvent);
    }

    public void onEventMainThread(@NonNull final SetupResponse setupResponse) {
        onSetupResponse(setupResponse);
    }

    @SuppressFBWarnings({"BC_UNCONFIRMED_CAST"})
    public void onEventMainThread(@NonNull final BillingResponse billingResponse) {
        onResponse(billingResponse);
        switch (billingResponse.getType()) {
            case PURCHASE:
                onPurchase((PurchaseResponse) billingResponse);
                break;
            case CONSUME:
                onConsume((ConsumeResponse) billingResponse);
                break;
            case INVENTORY:
                onInventory((InventoryResponse) billingResponse);
                break;
            case SKU_DETAILS:
                onSkuDetails((SkuDetailsResponse) billingResponse);
                break;
            default:
                throw new IllegalStateException();
        }
    }

    public void onEventMainThread(@NonNull final BillingRequest billingRequest) {
        onRequest(billingRequest);
    }

    @Override
    public void onRequest(@NonNull final BillingRequest billingRequest) {
        OPFLog.logMethod(billingRequest);
        final BillingListener billingListener = OPFIab.getConfiguration().getBillingListener();
        if (billingListener != null) {
            billingListener.onRequest(billingRequest);
        }
        super.onRequest(billingRequest);
    }

    @Override
    public void onResponse(@NonNull final BillingResponse billingResponse) {
        OPFLog.logMethod(billingResponse);
        final BillingListener billingListener = OPFIab.getConfiguration().getBillingListener();
        if (billingListener != null) {
            billingListener.onResponse(billingResponse);
        }
        super.onResponse(billingResponse);
    }

    @Override
    public void onSetupStarted(@NonNull final SetupStartedEvent setupStartedEvent) {
        OPFLog.logMethod(setupStartedEvent);
        final BillingListener billingListener = OPFIab.getConfiguration().getBillingListener();
        if (billingListener != null) {
            billingListener.onSetupStarted(setupStartedEvent);
        }
        super.onSetupStarted(setupStartedEvent);
    }

    @Override
    public void onSetupResponse(@NonNull final SetupResponse setupResponse) {
        OPFLog.logMethod(setupResponse);
        final BillingListener billingListener = OPFIab.getConfiguration().getBillingListener();
        if (billingListener != null) {
            billingListener.onSetupResponse(setupResponse);
        }
        super.onSetupResponse(setupResponse);
    }

    @Override
    public void onPurchase(@NonNull final PurchaseResponse purchaseResponse) {
        final BillingListener billingListener = OPFIab.getConfiguration().getBillingListener();
        if (billingListener != null) {
            billingListener.onPurchase(purchaseResponse);
        }
        super.onPurchase(purchaseResponse);
    }

    @Override
    public void onConsume(@NonNull final ConsumeResponse consumeResponse) {
        final BillingListener billingListener = OPFIab.getConfiguration().getBillingListener();
        if (billingListener != null) {
            billingListener.onConsume(consumeResponse);
        }
        super.onConsume(consumeResponse);
    }

    @Override
    public void onInventory(@NonNull final InventoryResponse inventoryResponse) {
        final BillingListener billingListener = OPFIab.getConfiguration().getBillingListener();
        if (billingListener != null) {
            billingListener.onInventory(inventoryResponse);
        }
        super.onInventory(inventoryResponse);
    }

    @Override
    public void onSkuDetails(@NonNull final SkuDetailsResponse skuDetailsResponse) {
        final BillingListener billingListener = OPFIab.getConfiguration().getBillingListener();
        if (billingListener != null) {
            billingListener.onSkuDetails(skuDetailsResponse);
        }
        super.onSkuDetails(skuDetailsResponse);
    }
}
