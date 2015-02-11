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
import org.onepf.opfiab.model.event.SetupResponse;
import org.onepf.opfiab.model.event.billing.Request;
import org.onepf.opfiab.model.event.billing.ConsumeResponse;
import org.onepf.opfiab.model.event.billing.InventoryResponse;
import org.onepf.opfiab.model.event.billing.PurchaseResponse;
import org.onepf.opfiab.model.event.billing.Response;
import org.onepf.opfiab.model.event.billing.SkuDetailsResponse;
import org.onepf.opfutils.OPFLog;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

final class GlobalBillingListener extends BillingListenerWrapper {

    GlobalBillingListener(@Nullable final BillingListener billingListener) {
        super(billingListener);
    }

    public void onEventMainThread(@NonNull final SetupResponse setupResponse) {
        onSetup(setupResponse);
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

    @Override
    public void onSetup(@NonNull final SetupResponse setupResponse) {
        OPFLog.methodD(setupResponse);
        super.onSetup(setupResponse);
    }

    @Override
    public void onRequest(@NonNull final Request request) {
        OPFLog.methodD(request);
        super.onRequest(request);
    }

    @Override
    public void onResponse(@NonNull final Response response) {
        OPFLog.methodD(response);
        super.onResponse(response);
    }
}
