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
import org.onepf.opfiab.listener.OnConsumeListener;
import org.onepf.opfiab.listener.OnInventoryListener;
import org.onepf.opfiab.listener.OnPurchaseListener;
import org.onepf.opfiab.listener.OnSetupListener;
import org.onepf.opfiab.listener.OnSkuDetailsListener;
import org.onepf.opfiab.model.event.SetupResponse;
import org.onepf.opfiab.model.event.billing.BillingRequest;
import org.onepf.opfiab.model.event.billing.BillingResponse;
import org.onepf.opfiab.model.event.billing.ConsumeResponse;
import org.onepf.opfiab.model.event.billing.InventoryResponse;
import org.onepf.opfiab.model.event.billing.PurchaseResponse;
import org.onepf.opfiab.model.event.billing.SkuDetailsResponse;
import org.onepf.opfutils.OPFChecks;
import org.onepf.opfutils.OPFLog;

import java.util.Collection;
import java.util.HashSet;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

final class BillingEventDispatcher extends BillingListenerCompositor {

    private final Collection<AdvancedIabHelper> helpers = new HashSet<>();

    BillingEventDispatcher(@Nullable final BillingListener billingListener) {
        super(billingListener == null
                      ? new BillingListener[0]
                      : new BillingListener[]{billingListener});
    }

    void register(@NonNull final AdvancedIabHelper helper) {
        OPFChecks.checkThread(true);
        helpers.add(helper);
    }

    void unregister(@NonNull final AdvancedIabHelper helper) {
        OPFChecks.checkThread(true);
        helpers.remove(helper);
    }

    public void onEventMainThread(@NonNull final SetupResponse setupResponse) {
        onSetup(setupResponse);
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
        super.onRequest(billingRequest);
    }

    @Override
    public void onResponse(@NonNull final BillingResponse billingResponse) {
        OPFLog.logMethod(billingResponse);
        super.onResponse(billingResponse);
    }

    @Override
    public void onSetup(@NonNull final SetupResponse setupResponse) {
        OPFLog.logMethod(setupResponse);
        super.onSetup(setupResponse);
        for (final AdvancedIabHelper helper : helpers) {
            for (final OnSetupListener listener : helper.setupListeners) {
                listener.onSetup(setupResponse);
            }
        }
    }

    @Override
    public void onPurchase(@NonNull final PurchaseResponse purchaseResponse) {
        super.onPurchase(purchaseResponse);
        for (final AdvancedIabHelper helper : helpers) {
            for (final OnPurchaseListener listener : helper.purchaseListeners) {
                listener.onPurchase(purchaseResponse);
            }
        }
    }

    @Override
    public void onConsume(@NonNull final ConsumeResponse consumeResponse) {
        super.onConsume(consumeResponse);
        for (final AdvancedIabHelper helper : helpers) {
            for (final OnConsumeListener listener : helper.consumeListeners) {
                listener.onConsume(consumeResponse);
            }
        }
    }

    @Override
    public void onInventory(@NonNull final InventoryResponse inventoryResponse) {
        super.onInventory(inventoryResponse);
        for (final AdvancedIabHelper helper : helpers) {
            for (final OnInventoryListener listener : helper.inventoryListeners) {
                listener.onInventory(inventoryResponse);
            }
        }
    }

    @Override
    public void onSkuDetails(@NonNull final SkuDetailsResponse skuDetailsResponse) {
        super.onSkuDetails(skuDetailsResponse);
        for (final AdvancedIabHelper helper : helpers) {
            for (final OnSkuDetailsListener listener : helper.skuDetailsListeners) {
                listener.onSkuDetails(skuDetailsResponse);
            }
        }
    }
}
