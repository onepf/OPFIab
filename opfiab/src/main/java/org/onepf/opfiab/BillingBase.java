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

import android.app.Application;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.onepf.opfiab.billing.BillingProvider;
import org.onepf.opfiab.model.Configuration;
import org.onepf.opfiab.model.event.RequestHandledEvent;
import org.onepf.opfiab.model.event.SetupResponse;
import org.onepf.opfiab.model.event.billing.BillingRequest;
import org.onepf.opfiab.model.event.billing.BillingResponse;
import org.onepf.opfiab.model.event.billing.Status;
import org.onepf.opfiab.util.OPFIabUtils;
import org.onepf.opfutils.OPFChecks;

import static org.onepf.opfiab.model.event.billing.Status.BILLING_UNAVAILABLE;
import static org.onepf.opfiab.model.event.billing.Status.BUSY;
import static org.onepf.opfiab.model.event.billing.Status.NO_BILLING_PROVIDER;

/**
 * This class is intended to be a single entry point for all {@link BillingRequest}s, it also holds
 * library state (current {@link BillingProvider}) and last {@link SetupResponse}.
 */
final class BillingBase {

    private static BillingBase instance;

    @SuppressWarnings({"PMD.NonThreadSafeSingleton"})
    static BillingBase getInstance() {
        OPFChecks.checkThread(true);
        if (instance == null) {
            instance = new BillingBase();
        }
        return instance;
    }


    /**
     * Currently used configuration object.
     *
     * @see OPFIab#init(Application, Configuration)
     */
    private Configuration configuration;
    /**
     * Last received setup response.
     *
     * @see OPFIab#setup()
     */
    @Nullable
    private SetupResponse setupResponse;
    /**
     * Currently used billing provider.
     */
    @Nullable
    private BillingProvider currentProvider;
    /**
     * Request being executed by {@link #currentProvider}.
     *
     * @see RequestHandledEvent
     */
    @Nullable
    private BillingRequest pendingRequest;

    private BillingBase() {
        super();
    }

    private void setCurrentProvider(@Nullable final BillingProvider provider) {
        if (currentProvider != null) {
            // Unregister provider from receiving any billing requests
            OPFIab.unregister(currentProvider);
        }
        currentProvider = provider;
        if (currentProvider != null) {
            OPFIab.register(currentProvider);
        }
    }

    private void postEmptyResponse(@NonNull final BillingRequest billingRequest,
                                   @NonNull final Status status) {
        OPFIab.post(OPFIabUtils.emptyResponse(null, billingRequest, status));
    }

    /**
     * Sets configuration currently used by library.
     * <p>
     * This method resets library setup state.
     *
     * @param configuration Current configuration object
     */
    void setConfiguration(@NonNull final Configuration configuration) {
        this.configuration = configuration;
        setCurrentProvider(null);
        setupResponse = null;
    }

    /**
     * Gets last setup response.
     *
     * @return SetupResponse object if setup has finished at least once, null otherwise.
     */
    @Nullable
    SetupResponse getSetupResponse() {
        OPFChecks.checkThread(true);
        return setupResponse;
    }

    /**
     * Gets request currently being executed.
     *
     * @return BillingRequest object if there's one, null otherwise.
     */
    @Nullable
    BillingRequest getPendingRequest() {
        OPFChecks.checkThread(true);
        return pendingRequest;
    }

    /**
     * Indicates whether current {@link BillingProvider} is busy executing request.
     *
     * @return True is BillingProvider is busy, false otherwise.
     */
    boolean isBusy() {
        OPFChecks.checkThread(true);
        return getPendingRequest() != null;
    }

    /**
     * Attempts to execute supplied billing request using current billing provider.
     * <p>
     * If current provider is unavailable or busy, supplied request will not be executed and
     * instead corresponding response will be send immediately.
     *
     * @param billingRequest BillingRequest to execute.
     * @see #isBusy()
     */
    void postRequest(@NonNull final BillingRequest billingRequest) {
        OPFChecks.checkThread(true);
        final SetupResponse setupResponse;
        if (isBusy()) {
            // Library is busy with another request
            postEmptyResponse(billingRequest, BUSY);
        } else if ((setupResponse = getSetupResponse()) == null || !setupResponse.isSuccessful()) {
            // Setup was not started, is in progress or failed
            postEmptyResponse(billingRequest, NO_BILLING_PROVIDER);
        } else {
            pendingRequest = billingRequest;
            // Send request to be handled by BillingProvider
            OPFIab.post(billingRequest);
        }
    }

    public void onEventMainThread(@NonNull final SetupResponse setupResponse) {
        // Called before any other SetupResponse handler
        this.setupResponse = setupResponse;
        if (setupResponse.isSuccessful()) {
            // Suitable provider was found
            setCurrentProvider(setupResponse.getBillingProvider());
        }
    }

    public void onEventMainThread(@NonNull final RequestHandledEvent event) {
        if (!event.getBillingRequest().equals(pendingRequest)) {
            // For some reason billing provider didn't report correct request
            throw new IllegalStateException();
        }
        pendingRequest = null;
    }

    public void onEventMainThread(@NonNull final BillingResponse billingResponse) {
        // Current provider is set but is not available
        if (currentProvider != null && billingResponse.getStatus() == BILLING_UNAVAILABLE
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
