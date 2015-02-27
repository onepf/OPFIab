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

import org.onepf.opfiab.model.event.RequestHandledEvent;
import org.onepf.opfiab.model.event.SetupResponse;
import org.onepf.opfiab.model.event.billing.BillingRequest;
import org.onepf.opfutils.OPFChecks;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

public class ScheduledIabHelper extends IabHelperAdapter {

    private static final Map<IabHelperAdapter, Set<BillingRequest>> HELPERS =
            new WeakHashMap<>();
    private static final Handler HANDLER = new Handler(Looper.getMainLooper());
    private static final Runnable HANDLE_NEXT_REQUESTS = new Runnable() {
        @Override
        public void run() {
            final BaseIabHelper baseIabHelper = OPFIab.getBaseHelper();
            final boolean setupFinished = baseIabHelper.getSetupResponse() != null;
            if (!setupFinished) {
                OPFIab.setup();
            } else if (!baseIabHelper.isBusy()) {
                // Look for pending request
                for (final Map.Entry<IabHelperAdapter, Set<BillingRequest>> entry : HELPERS.entrySet()) {
                    final Set<BillingRequest> requests = entry.getValue();
                    if (!requests.isEmpty()) {
                        final Iterator<BillingRequest> requestIterator = requests.iterator();
                        final IabHelperAdapter helperAdapter = entry.getKey();
                        helperAdapter.iabHelper.postRequest(requestIterator.next());
                        requestIterator.remove();
                        return;
                    }
                }
            }
        }
    };
    private static final Object EVENT_HANDLER = new Object() {

        public void onEventMainThread(@NonNull final SetupResponse setupResponse) {
            HANDLE_NEXT_REQUESTS.run();
        }

        public final void onEventMainThread(@NonNull final RequestHandledEvent event) {
            final long requestDelay = OPFIab.getConfiguration().getSubsequentRequestDelay();
            HANDLER.removeCallbacks(HANDLE_NEXT_REQUESTS);
            HANDLER.postDelayed(HANDLE_NEXT_REQUESTS, requestDelay);        }
    };


    private final Set<BillingRequest> requests = new LinkedHashSet<>();

    public ScheduledIabHelper(@NonNull final IabHelper iabHelper) {
        super(iabHelper);
        OPFIab.register(EVENT_HANDLER);
        HELPERS.put(this, requests);
    }

    @Override
    protected void postRequest(@NonNull final BillingRequest billingRequest) {
        OPFChecks.checkThread(true);
        requests.add(billingRequest);
        HANDLE_NEXT_REQUESTS.run();
    }

    public void flush() {
        OPFChecks.checkThread(true);
        requests.clear();
    }
}
