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

import java.util.Collection;
import java.util.LinkedHashSet;

final class BillingRequestScheduler {

    private final long requestDelay = OPFIab.getConfiguration().getSubsequentRequestDelay();
    private final IabHelperBase helperBase = OPFIab.getBase();
    private final Collection<AdvancedIabHelper> helpersQueue = new LinkedHashSet<>();
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable handleNextRequest = new Runnable() {
        @Override
        public void run() {
            AdvancedIabHelper advancedIabHelper;
            while ((advancedIabHelper = OPFIabUtils.poll(helpersQueue)) != null) {
                if (advancedIabHelper.postNextRequest()) {
                    return;
                }
            }
        }
    };


    BillingRequestScheduler() {
        super();
    }

    private void handleOrSchedule() {
        handler.removeCallbacks(handleNextRequest);
        if (helperBase.getSetupResponse() == null) {
            OPFIab.setup();
        } else if (!helperBase.isBusy()) {
            handleNextRequest.run();
        } else {
            handler.postDelayed(handleNextRequest, requestDelay);
        }
    }

    void schedule(@NonNull final AdvancedIabHelper helper) {
        helpersQueue.add(helper);
        handleOrSchedule();
    }

    public void onEventMainThread(@NonNull final SetupResponse setupResponse) {
        if (setupResponse.isSuccessful()) {
            handleOrSchedule();
        }
    }

    @SuppressWarnings("UnusedParameters")
    public void onEventMainThread(@NonNull final RequestHandledEvent event) {
        handleOrSchedule();
    }
}
