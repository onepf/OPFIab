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
import android.util.SparseArray;

import org.onepf.opfiab.model.Configuration;
import org.onepf.opfiab.model.billing.Purchase;
import org.onepf.opfiab.model.event.ActivityResultEvent;
import org.onepf.opfiab.model.event.RequestHandledEvent;
import org.onepf.opfiab.model.event.SetupResponse;
import org.onepf.opfiab.model.event.billing.ConsumeRequest;
import org.onepf.opfiab.model.event.billing.InventoryRequest;
import org.onepf.opfiab.model.event.billing.PurchaseRequest;
import org.onepf.opfiab.model.event.billing.Request;
import org.onepf.opfiab.model.event.billing.Response;
import org.onepf.opfiab.model.event.billing.SkuDetailsRequest;
import org.onepf.opfutils.OPFChecks;
import org.onepf.opfutils.OPFLog;

import java.util.Arrays;
import java.util.Set;

import static org.onepf.opfiab.model.event.BillingEvent.Type;
import static org.onepf.opfiab.model.event.billing.Response.Status.BILLING_UNAVAILABLE;
import static org.onepf.opfiab.model.event.billing.Response.Status.BUSY;

final class BaseIabHelper extends IabHelper {

    private final Configuration configuration = OPFIab.getConfiguration();
    private final SparseArray<Long> requestsTiming;
    @Nullable
    private SetupResponse setupResponse = null;
    @Nullable
    private BillingProvider currentProvider = null;
    @Nullable
    private Request pendingRequest;

    BaseIabHelper() {
        final Type[] types = Type.values();
        requestsTiming = new SparseArray<>(types.length);
        for (final Type type : types) {
            requestsTiming.put(type.ordinal(), 0L);
        }

        OPFIab.register(new Object() {
            public final void onEventMainThread(@NonNull final RequestHandledEvent event) {
                // At this point request should be handled by BillingProvider
                if (pendingRequest != event.getRequest()) {
                    throw new IllegalStateException();
                }
                pendingRequest = null;
            }
        }, Integer.MIN_VALUE);
    }

    private void setCurrentProvider(@Nullable final BillingProvider provider) {
        if (currentProvider != null) {
            OPFIab.unregister(currentProvider);
        }
        currentProvider = provider;
        if (currentProvider != null) {
            OPFIab.register(currentProvider);
        }
    }

    private void postEmptyResponse(@NonNull final Request request,
                                   @NonNull Response.Status status) {
        OPFIab.post(OPFIabUtils.emptyResponse(null, request.getType(), status));
    }

    private void postRequest(@NonNull final Request request) {
        OPFChecks.checkThread(true);

        final int type = request.getType().ordinal();
        // Check if it's too soon to handle this type of request
        final long requestGap = configuration.getSameTypeRequestGap();
        if (requestGap > 0L) {
            final long lastTime = requestsTiming.get(type);
            if (System.currentTimeMillis() - lastTime < requestGap) {
                postEmptyResponse(request, BUSY);
                return;
            }
        }

        if (pendingRequest != null) {
            // Library is busy with another request
            postEmptyResponse(request, BUSY);
        } else if (setupResponse == null) {
            // Setup is not yet started or is in progress
            // Start lazy setup
            pendingRequest = request;
            OPFIab.setup();
        } else if (currentProvider == null) {
            // Setup is finished, but there's no suitable BillingProvider
            postEmptyResponse(request, BILLING_UNAVAILABLE);
        } else if (!currentProvider.isAvailable()) {
            // BillingProvider is no longer available
            OPFLog.e("BillingProvider is no longer available!\n%s", currentProvider);
            if (configuration.autoRecover()) {
                // Try to pick new BillingProvider
                setCurrentProvider(null);
                setupResponse = null;
                pendingRequest = request;
                OPFIab.setup();
            } else {
                // Can't handle requests until current BillingProvider becomes available again
                postEmptyResponse(request, BILLING_UNAVAILABLE);
            }
        } else {
            // Send request to be handled by BillingProvider
            pendingRequest = request;
            OPFIab.post(request);
            requestsTiming.put(type, System.currentTimeMillis());
        }
    }

    @Nullable
    SetupResponse getSetupResponse() {
        OPFChecks.checkThread(true);
        return setupResponse;
    }

    public void onEventMainThread(@NonNull final SetupResponse setupResponse) {
        this.setupResponse = setupResponse;
        if (setupResponse.isSuccessful()) {
            setCurrentProvider(setupResponse.getBillingProvider());
        }
        if (pendingRequest != null) {
            final Request request = pendingRequest;
            pendingRequest = null;
            postRequest(request);
        }
    }

    @Override
    public void purchase(@NonNull final Activity activity,
                         @NonNull final String sku) {
        OPFLog.methodD(activity, sku);
        postRequest(new PurchaseRequest(activity, sku));
    }

    @Override
    public void consume(@NonNull final Purchase purchase) {
        OPFLog.methodD(purchase);
        postRequest(new ConsumeRequest(purchase));
    }

    @Override
    public void inventory() {
        OPFLog.methodD();
        postRequest(new InventoryRequest());
    }

    @Override
    public void skuDetails(@NonNull final Set<String> skus) {
        OPFLog.methodD(Arrays.toString(skus.toArray()));
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
