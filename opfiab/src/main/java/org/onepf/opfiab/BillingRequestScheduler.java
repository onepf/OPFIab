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

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.onepf.opfiab.api.IabHelper;
import org.onepf.opfiab.model.Configuration;
import org.onepf.opfiab.model.event.RequestHandledEvent;
import org.onepf.opfiab.model.event.SetupResponse;
import org.onepf.opfiab.model.event.billing.BillingRequest;
import org.onepf.opfiab.util.OPFIabUtils;
import org.onepf.opfutils.OPFChecks;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;

/**
 * This class is responsible for pending {@link BillingRequest}s execution. It monitors {@link
 * BillingBase} state changes and notifies known {@link IabHelper}s when next request can be
 * handled.
 */
final class BillingRequestScheduler {

    @Nullable
    private static BillingRequestScheduler instance;

    @SuppressWarnings({"PMD.NonThreadSafeSingleton"})
    static BillingRequestScheduler getInstance() {
        OPFChecks.checkThread(true);
        if (instance == null) {
            instance = new BillingRequestScheduler();
        }
        return instance;
    }


    private final Handler handler = new Handler(Looper.getMainLooper());
    /**
     * Collection of helpers potentially having pending requests.
     */
    private final Map<IabHelperImpl, Collection<BillingRequest>> helpers = new HashMap<>();
    @SuppressWarnings("OverlyComplexAnonymousInnerClass")
    private final Runnable handleNextRequest = new Runnable() {
        @Override
        public void run() {
            // Iterate through registered helpers looking for pending request
            for (final Map.Entry<IabHelperImpl, Collection<BillingRequest>> entry : helpers.entrySet()) {
                final IabHelperImpl helper = entry.getKey();
                if (helper.billingBase.isBusy()) {
                    // Library is busy, pending requests will have to wait some more.
                    return;
                }
                final BillingRequest request = OPFIabUtils.poll(entry.getValue());
                if (request != null) {
                    // Send request for execution
                    entry.getKey().postRequest(request);
                    return;
                }
            }
        }
    };


    private BillingRequestScheduler() {
        super();
    }

    /**
     * Registers callback which will attempt to execute enqueued request after certain delay.
     *
     * @see Configuration#getSubsequentRequestDelay()
     */
    private void schedule() {
        handler.removeCallbacks(handleNextRequest);
        final long delay = OPFIab.getConfiguration().getSubsequentRequestDelay();
        handler.postDelayed(handleNextRequest, delay);
    }

    /**
     * Checks if supplied request is present in any known helpers queue. If not it will be enqueued
     * for later execution and skipped otherwise.
     *
     * @param helper  Helper initially responsible for supplied request.
     * @param request Request object to try to add to queue.
     */
    void schedule(@NonNull final IabHelperImpl helper, @NonNull final BillingRequest request) {
        for (final Collection<BillingRequest> requests : helpers.values()) {
            if (requests.contains(request)) {
                // Request is already in queue.
                return;
            }
        }

        final Collection<BillingRequest> queue;
        if (!helpers.containsKey(helper)) {
            helpers.put(helper, queue = new LinkedHashSet<>());
        } else {
            queue = helpers.get(helper);
        }
        queue.add(request);
        schedule();
    }

    /**
     * Dismisses all pending requests associated with the supplied helper.
     *
     * @param iabHelper Helper which request queue should be dismissed.
     */
    void dropQueue(@NonNull final AdvancedIabHelperImpl iabHelper) {
        helpers.remove(iabHelper);
    }

    /**
     * Dismisses all pending requests for all known helpers.
     */
    void dropQueue() {
        handler.removeCallbacks(handleNextRequest);
        helpers.clear();
    }

    @SuppressWarnings("UnusedParameters")
    public void onEventMainThread(@NonNull final RequestHandledEvent event) {
        schedule();
    }

    @SuppressWarnings("UnusedParameters")
    public void onEventMainThread(@NonNull final SetupResponse setupResponse) {
        handleNextRequest.run();
    }
}
