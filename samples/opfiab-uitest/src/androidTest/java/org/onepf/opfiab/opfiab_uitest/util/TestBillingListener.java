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

package org.onepf.opfiab.opfiab_uitest.util;

import android.support.annotation.NonNull;

import org.junit.Assert;
import org.onepf.opfiab.listener.BillingListener;
import org.onepf.opfiab.model.event.SetupResponse;
import org.onepf.opfiab.model.event.SetupStartedEvent;
import org.onepf.opfiab.model.event.billing.BillingRequest;
import org.onepf.opfiab.model.event.billing.BillingResponse;
import org.onepf.opfiab.model.event.billing.ConsumeResponse;
import org.onepf.opfiab.model.event.billing.InventoryResponse;
import org.onepf.opfiab.model.event.billing.PurchaseResponse;
import org.onepf.opfiab.model.event.billing.SkuDetailsResponse;
import org.onepf.opfutils.OPFLog;

/**
 * @author antonpp
 * @since 02.06.15
 */
public class TestBillingListener implements BillingListener {

    private boolean failOnReceive;

    public TestBillingListener() {
        failOnReceive = false;
    }

    public TestBillingListener(final boolean failOnReceive) {
        this.failOnReceive = failOnReceive;
    }

    public TestBillingListener setFailOnReceive(final boolean failOnReceive) {
        this.failOnReceive = failOnReceive;
        return this;
    }

    @Override
    public void onRequest(@NonNull final BillingRequest billingRequest) {
        fail();
    }

    private void fail() {
        if (failOnReceive) {
            OPFLog.e("Received unexpected event");
            Assert.fail();
        }
    }

    @Override
    public void onResponse(@NonNull final BillingResponse billingResponse) {
        fail();
    }

    @Override
    public void onConsume(@NonNull final ConsumeResponse consumeResponse) {
        fail();
    }

    @Override
    public void onInventory(@NonNull final InventoryResponse inventoryResponse) {
        fail();
    }

    @Override
    public void onSetupStarted(@NonNull final SetupStartedEvent setupStartedEvent) {
        fail();
    }

    @Override
    public void onSetupResponse(@NonNull final SetupResponse setupResponse) {
        fail();
    }

    @Override
    public void onSkuDetails(@NonNull final SkuDetailsResponse skuDetailsResponse) {
        fail();
    }

    @Override
    public void onPurchase(@NonNull final PurchaseResponse purchaseResponse) {
        fail();
    }
}
