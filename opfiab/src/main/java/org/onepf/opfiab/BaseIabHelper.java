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

import org.onepf.opfiab.billing.BillingProvider;
import org.onepf.opfiab.model.Configuration;
import org.onepf.opfiab.model.event.RequestHandledEvent;
import org.onepf.opfiab.model.event.SetupResponse;
import org.onepf.opfiab.model.event.billing.BillingRequest;
import org.onepf.opfiab.model.event.billing.BillingResponse;
import org.onepf.opfiab.model.event.billing.Status;
import org.onepf.opfutils.OPFChecks;

import static org.onepf.opfiab.model.event.billing.Status.BILLING_UNAVAILABLE;
import static org.onepf.opfiab.model.event.billing.Status.BUSY;
import static org.onepf.opfiab.model.event.billing.Status.NO_BILLING_PROVIDER;

final class BaseIabHelper extends IabHelper {

    private final Configuration configuration = OPFIab.getConfiguration();
    @Nullable
    private SetupResponse setupResponse = null;
    @Nullable
    private BillingProvider currentProvider = null;
    @Nullable
    private BillingRequest pendingRequest;

    BaseIabHelper() { }

    private void setCurrentProvider(@Nullable final BillingProvider provider) {
        if (currentProvider != null) {
            OPFIab.unregister(currentProvider);
        }
        currentProvider = provider;
        if (currentProvider != null) {
            OPFIab.register(currentProvider);
        }
    }

    private void postEmptyResponse(@NonNull final BillingRequest billingRequest,
                                   @NonNull Status status) {
        OPFIab.post(OPFIabUtils.emptyResponse(null, billingRequest, status));
    }

    @Nullable
    SetupResponse getSetupResponse() {
        OPFChecks.checkThread(true);
        return setupResponse;
    }

    boolean isBusy() {
        OPFChecks.checkThread(true);
        return pendingRequest != null;
    }

    @Override
    protected void postRequest(@NonNull final BillingRequest billingRequest) {
        OPFChecks.checkThread(true);
        final SetupResponse setupResponse;
        if (isBusy()) {
            // Library is busy with another request
            postEmptyResponse(billingRequest, BUSY);
        } else if ((setupResponse = getSetupResponse()) == null || !setupResponse.isSuccessful()) {
            // Setup was not started, is in progress or failed
            postEmptyResponse(billingRequest, NO_BILLING_PROVIDER);
        } else {
            // Send request to be handled by BillingProvider
            pendingRequest = billingRequest;
            OPFIab.post(billingRequest);
        }
    }

    public void onEventMainThread(@NonNull final SetupResponse setupResponse) {
        this.setupResponse = setupResponse;
        if (setupResponse.isSuccessful()) {
            setCurrentProvider(setupResponse.getBillingProvider());
        }
        if (pendingRequest != null) {
            final BillingRequest billingRequest = pendingRequest;
            pendingRequest = null;
            postRequest(billingRequest);
        }
    }

    public final void onEventMainThread(@NonNull final RequestHandledEvent event) {
        // At this point request should be handled by BillingProvider
        if (pendingRequest != event.getBillingRequest()) {
            throw new IllegalStateException();
        }
        pendingRequest = null;
    }

    public void onEventMainThread(@NonNull final BillingResponse billingResponse) {
        // Current provider is set but is not available
        if (currentProvider != null && (billingResponse.getStatus() == BILLING_UNAVAILABLE)
                // However last setup attempt was successful
                && setupResponse != null && setupResponse.isSuccessful()
                // Auto-recovery is set
                && configuration.autoRecover()) {
            // Attempt to pick new billing provider
            setCurrentProvider(null);
            setupResponse = null;
            OPFIab.setup();
        }
    }
}
