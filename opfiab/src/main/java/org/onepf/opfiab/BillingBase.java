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
import org.onepf.opfiab.model.event.SetupStartedEvent;
import org.onepf.opfiab.model.event.billing.BillingRequest;
import org.onepf.opfiab.model.event.billing.BillingResponse;
import org.onepf.opfiab.util.BillingUtils;
import org.onepf.opfiab.util.OPFIabUtils;
import org.onepf.opfutils.OPFChecks;
import org.onepf.opfutils.OPFLog;

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

    /**
     * Sets configuration currently used by library.
     * <p/>
     * This method resets library setup state.
     *
     * @param configuration Current configuration object
     */
    void setConfiguration(@NonNull final Configuration configuration) {
        this.configuration = configuration;
        this.setupResponse = null;
        this.currentProvider = null;
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
     * <p/>
     * If current provider is unavailable or busy, supplied request will not be executed and
     * instead corresponding response will be send immediately.
     *
     * @param billingRequest BillingRequest to execute.
     *
     * @see #isBusy()
     */
    void postRequest(@NonNull final BillingRequest billingRequest) {
        OPFChecks.checkThread(true);
        final SetupResponse setupResponse;
        if (isBusy()) {
            // Library is busy with another request
            OPFIab.post(BillingUtils.emptyResponse(null, billingRequest, BUSY));
        } else if ((setupResponse = getSetupResponse()) == null || !setupResponse.isSuccessful()) {
            // Setup was not started, is in progress or failed
            OPFIab.post(BillingUtils.emptyResponse(null, billingRequest, NO_BILLING_PROVIDER));
        } else if (configuration.skipStaleRequests() && OPFIabUtils.isStale(billingRequest)) {
            // Request is no longer relevant, try next one
            OPFLog.d("Skipping stale request: " + billingRequest);
            BillingRequestScheduler.getInstance().handleNext();
        } else {
            pendingRequest = billingRequest;
            // Send request to be handled by BillingProvider
            OPFIab.post(billingRequest);
        }
    }

    public void onEvent(@NonNull final SetupStartedEvent event) {
        OPFChecks.checkThread(true);
        this.currentProvider = null;
        this.setupResponse = null;
    }

    public void onEventMainThread(@NonNull final SetupResponse setupResponse) {
        // Called before any other SetupResponse handler
        this.setupResponse = setupResponse;
        if (setupResponse.isSuccessful()) {
            // Suitable provider was found
            currentProvider = setupResponse.getBillingProvider();
        }
    }

    public void onEventAsync(@NonNull final BillingRequest billingRequest) {
        final BillingProvider billingProvider = this.currentProvider;
        if (billingProvider != null) {
            billingProvider.onBillingRequest(billingRequest);
        }
        OPFIab.post(new RequestHandledEvent(billingRequest));
    }

    public void onEventMainThread(@NonNull final RequestHandledEvent event) {
        if (!event.getBillingRequest().equals(pendingRequest)) {
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
            OPFIab.setup();
        }
    }
}
