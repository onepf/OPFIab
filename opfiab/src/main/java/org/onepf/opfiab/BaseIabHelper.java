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

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.onepf.opfiab.model.BillingProviderInfo;
import org.onepf.opfiab.model.billing.ConsumableDetails;
import org.onepf.opfiab.model.billing.SkuDetails;
import org.onepf.opfiab.model.event.ActivityResultEvent;
import org.onepf.opfiab.model.event.BillingEvent;
import org.onepf.opfiab.model.event.SetupEvent;
import org.onepf.opfiab.model.event.request.ConsumeRequest;
import org.onepf.opfiab.model.event.request.InventoryRequest;
import org.onepf.opfiab.model.event.request.PurchaseRequest;
import org.onepf.opfiab.model.event.request.Request;
import org.onepf.opfiab.model.event.request.SkuDetailsRequest;
import org.onepf.opfiab.model.event.response.Response;
import org.onepf.opfutils.OPFChecks;

import java.util.Collection;

import static org.onepf.opfiab.model.event.response.Response.Status.BILLING_UNAVAILABLE;
import static org.onepf.opfiab.model.event.response.Response.Status.BUSY;

final class BaseIabHelper extends IabHelper {

    @Nullable
    private BillingProvider currentProvider;
    @Nullable
    private Request pendingRequest;

    BaseIabHelper() { }

    public void onEventMainThread(@NonNull final SetupEvent event) {
        if (event.isSuccessful()) {
            currentProvider = event.getBillingProvider();
            eventBus.register(currentProvider);
        }
        if (pendingRequest != null) {
            postRequest(pendingRequest);
            pendingRequest = null;
        }
    }

    void postRequest(@NonNull final Request request) {
        OPFChecks.checkThread(true);
        final SetupManager.State state = SetupManager.getState();
        if (state != SetupManager.State.FINISHED) {
            if (state == SetupManager.State.INITIAL) {
                SetupManager.setup();
            }
            if (pendingRequest != null) {
                eventBus.post(OPFIabUtils.emptyResponse(null, request, BUSY));
            } else {
                pendingRequest = request;
            }
            return;
        }
        if (currentProvider == null || !currentProvider.isAvailable()) {
            eventBus.post(OPFIabUtils.emptyResponse(null, request, BILLING_UNAVAILABLE));
        } else if (eventBus.getStickyEvent(BillingEvent.class) instanceof Request) {
            final BillingProviderInfo info = currentProvider.getInfo();
            final Response response = OPFIabUtils.emptyResponse(info, request, BUSY);
            eventBus.post(response);
        } else {
            eventBus.postSticky(request);
        }
    }

    //TODO lazy initialized setup
    @Override
    public void purchase(@NonNull final Activity activity,
                         @NonNull final SkuDetails skuDetails) {
        postRequest(new PurchaseRequest(activity, skuDetails));
    }

    @Override
    public void consume(@NonNull final ConsumableDetails consumableDetails) {
        postRequest(new ConsumeRequest(consumableDetails));
    }

    @Override
    public void inventory() {
        postRequest(new InventoryRequest());
    }

    @Override
    public void skuDetails(@NonNull final Collection<String> skus) {
        postRequest(new SkuDetailsRequest(skus));
    }

    @Override
    public void onActivityResult(@NonNull final Activity activity, final int requestCode,
                                 final int resultCode,
                                 @Nullable final Intent data) {
        eventBus.post(new ActivityResultEvent(activity, requestCode, resultCode, data));
    }
}
