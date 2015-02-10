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
import org.onepf.opfiab.model.Configuration;
import org.onepf.opfiab.model.billing.Purchase;
import org.onepf.opfiab.model.event.ActivityResultEvent;
import org.onepf.opfiab.model.event.BillingEvent;
import org.onepf.opfiab.model.event.SetupResponse;
import org.onepf.opfiab.model.event.billing.ConsumeRequest;
import org.onepf.opfiab.model.event.billing.InventoryRequest;
import org.onepf.opfiab.model.event.billing.PurchaseRequest;
import org.onepf.opfiab.model.event.billing.Request;
import org.onepf.opfiab.model.event.billing.Response;
import org.onepf.opfiab.model.event.billing.SkuDetailsRequest;
import org.onepf.opfutils.OPFChecks;
import org.onepf.opfutils.OPFLog;

import java.util.Set;

import static org.onepf.opfiab.model.event.billing.Response.Status.BILLING_UNAVAILABLE;
import static org.onepf.opfiab.model.event.billing.Response.Status.BUSY;

final class BaseIabHelper extends IabHelper {

    @Nullable
    private BillingProvider currentProvider = null;
    @Nullable
    private Request pendingRequest;

    BaseIabHelper() { }

    private void setCurrentProvider(@Nullable final BillingProvider provider) {
        OPFChecks.checkThread(true);
        //noinspection ConstantConditions
        if (currentProvider != null) {
            OPFIab.unregister(currentProvider);
        }
        currentProvider = provider;
        OPFIab.register(currentProvider);
    }

    private void lazySetup(@NonNull final Request request) {
        if (pendingRequest != null) {
            OPFIab.post(OPFIabUtils.emptyResponse(null, request, BUSY));
        } else {
            pendingRequest = request;
        }
        OPFIab.setup();
    }

    private void postRequest(@NonNull final Request request) {
        OPFLog.methodD(request);
        OPFChecks.checkThread(true);
        if (OPFIab.getStickyEvent(SetupResponse.class) == null) {
            lazySetup(request);
        } else if (currentProvider == null) {
            OPFIab.post(OPFIabUtils.emptyResponse(null, request, BILLING_UNAVAILABLE));
        } else if (!currentProvider.isAvailable()) {
            OPFLog.e("BillingProvider is no longer available!\n%s", currentProvider);
            final Configuration configuration = OPFIab.getConfiguration();
            if (configuration.autoRecover()) {
                setCurrentProvider(null);
                lazySetup(request);
            } else {
                OPFIab.post(OPFIabUtils.emptyResponse(null, request, BILLING_UNAVAILABLE));
            }
        } else if (OPFIab.getStickyEvent(Request.class) != null) {
            final BillingProviderInfo info = currentProvider.getInfo();
            OPFIab.post(OPFIabUtils.emptyResponse(info, request, BUSY));
        } else {
            OPFIab.postSticky(request);
        }
    }

    public void onEventMainThread(@NonNull final SetupResponse setupResponse) {
        if (setupResponse.isSuccessful()) {
            setCurrentProvider(setupResponse.getBillingProvider());
        }
        if (pendingRequest != null) {
            postRequest(pendingRequest);
            pendingRequest = null;
        }
    }

    //TODO lazy initialized setup
    @Override
    public void purchase(@NonNull final Activity activity,
                         @NonNull final String sku) {
        postRequest(new PurchaseRequest(activity, sku));
    }

    @Override
    public void consume(@NonNull final Purchase purchase) {
        postRequest(new ConsumeRequest(purchase));
    }

    @Override
    public void inventory() {
        postRequest(new InventoryRequest());
    }

    @Override
    public void skuDetails(@NonNull final Set<String> skus) {
        postRequest(new SkuDetailsRequest(skus));
    }

    @Override
    public void onActivityResult(@NonNull final Activity activity,
                                 final int requestCode,
                                 final int resultCode,
                                 @Nullable final Intent data) {
        OPFIab.post(new ActivityResultEvent(activity, requestCode, resultCode, data));
    }
}
