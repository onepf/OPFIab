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

import org.onepf.opfiab.misc.OPFIabUtils;
import org.onepf.opfiab.model.event.RequestHandledEvent;
import org.onepf.opfiab.model.event.SetupResponse;
import org.onepf.opfiab.model.event.billing.BillingRequest;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;

final class BillingRequestScheduler {

    private final long requestDelay = OPFIab.getConfiguration().getSubsequentRequestDelay();
    private final BillingBase billingBase = OPFIab.getBase();
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Map<IabHelper, Collection<BillingRequest>> helpers = new HashMap<>();
    @SuppressWarnings("OverlyComplexAnonymousInnerClass")
    private final Runnable handleNextRequest = new Runnable() {
        @Override
        public void run() {
            if (billingBase.isBusy()) {
                return;
            }
            for (final Map.Entry<IabHelper, Collection<BillingRequest>> entry : helpers.entrySet()) {
                final BillingRequest request = OPFIabUtils.poll(entry.getValue());
                if (request != null) {
                    entry.getKey().postRequest(request);
                    return;
                }
            }
        }
    };


    BillingRequestScheduler() {
        super();
    }

    private void schedule() {
        handler.removeCallbacks(handleNextRequest);
        handler.postDelayed(handleNextRequest, requestDelay);
    }

    void schedule(@NonNull final IabHelper helper, @NonNull final BillingRequest request) {
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

    void dropQueue(@NonNull final AdvancedIabHelper iabHelper) {
        helpers.remove(iabHelper);
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
