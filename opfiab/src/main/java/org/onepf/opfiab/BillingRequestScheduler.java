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
import org.onepf.opfiab.model.event.billing.BillingResponse;

import java.util.Collection;
import java.util.LinkedHashSet;

final class BillingRequestScheduler {

    private final long requestDelay = OPFIab.getConfiguration().getSubsequentRequestDelay();
    private final BillingBase billingBase = OPFIab.getBase();
    private final Collection<AdvancedIabHelper> helpers = new LinkedHashSet<>();
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable handleNextRequest = new Runnable() {
        @Override
        public void run() {
            for (final AdvancedIabHelper iabHelper : helpers) {
                if (iabHelper.postNextRequest()) {
                    return;
                }
            }
        }
    };


    BillingRequestScheduler() {
        super();
    }

    void handleNextOrSchedule() {
        handler.removeCallbacks(handleNextRequest);
        if (billingBase.getSetupResponse() == null) {
            OPFIab.setup();
        } else if (billingBase.isBusy()) {
            handler.postDelayed(handleNextRequest, requestDelay);
        } else {
            handleNextRequest.run();
        }
    }

    void register(@NonNull final AdvancedIabHelper iabHelper) {
        helpers.add(iabHelper);
    }

    void unregister(@NonNull final AdvancedIabHelper iabHelper) {
        helpers.remove(iabHelper);
    }

    public void onEventMainThread(@NonNull final SetupResponse setupResponse) {
        if (setupResponse.isSuccessful()) {
            handleNextOrSchedule();
        }
    }

    @SuppressWarnings("UnusedParameters")
    public void onEventMainThread(@NonNull final RequestHandledEvent event) {
        handleNextOrSchedule();
    }

    @SuppressWarnings("UnusedParameters")
    public void onEventMainThread(@NonNull final BillingResponse event) {
        handleNextOrSchedule();
    }
}
