/*
 * Copyright 2012-2014 One Platform Foundation
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
import org.onepf.opfiab.listener.BillingListenerWrapper;
import org.onepf.opfiab.model.event.SetupEvent;
import org.onepf.opfiab.model.event.request.Request;
import org.onepf.opfiab.model.event.response.ConsumeResponse;
import org.onepf.opfiab.model.event.response.InventoryResponse;
import org.onepf.opfiab.model.event.response.PurchaseResponse;
import org.onepf.opfiab.model.event.response.Response;
import org.onepf.opfiab.model.event.response.SkuDetailsResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

final class GlobalBillingListener extends BillingListenerWrapper {

    @NonNull
    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalBillingListener.class);


    GlobalBillingListener(@Nullable final BillingListener billingListener) {
        super(billingListener);
        OPFIab.getEventBus().register(this);
    }

    public void onEventMainThread(@NonNull final SetupEvent setupEvent) {
        onSetup(setupEvent);
    }

    @SuppressFBWarnings({"BC_UNCONFIRMED_CAST"})
    public void onEventMainThread(@NonNull final Response response) {
        onResponse(response);
        switch (response.getType()) {
            case PURCHASE:
                onPurchase((PurchaseResponse) response);
                break;
            case CONSUME:
                onConsume((ConsumeResponse) response);
                break;
            case INVENTORY:
                onInventory((InventoryResponse) response);
                break;
            case SKU_DETAILS:
                onSkuDetails((SkuDetailsResponse) response);
                break;
        }
    }

    public void onEventMainThread(@NonNull final Request request) {
        onRequest(request);
    }
}
