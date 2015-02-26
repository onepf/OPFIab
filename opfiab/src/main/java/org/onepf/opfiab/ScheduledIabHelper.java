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

import org.onepf.opfiab.model.event.billing.BillingRequest;
import org.onepf.opfutils.OPFChecks;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

public class ScheduledIabHelper extends IabHelperAdapter {

    private static final Handler HANDLER = new Handler(Looper.getMainLooper());


    private final BaseIabHelper baseIabHelper = OPFIab.getBaseHelper();
    private final long delay = OPFIab.getConfiguration().getSubsequentRequestDelay();
    private final Set<BillingRequest> requests = new LinkedHashSet<>();
    private final Runnable handleNextRequest = new Runnable() {
        @Override
        public void run() {
            final boolean setupFinished = baseIabHelper.getSetupResponse() != null;
            if (!setupFinished) {
                OPFIab.setup();
            } else if (!baseIabHelper.isBusy() && !requests.isEmpty()) {
                final Iterator<BillingRequest> requestIterator = requests.iterator();
                ScheduledIabHelper.super.postRequest(requestIterator.next());
                requestIterator.remove();
            }
            if (!requests.isEmpty()) {
                HANDLER.postDelayed(this, delay);
            }
        }
    };

    public ScheduledIabHelper(@NonNull final IabHelper iabHelper) {
        super(iabHelper);
    }

    public void flush() {
        OPFChecks.checkThread(true);
        requests.clear();
        HANDLER.removeCallbacks(handleNextRequest);
    }

    @Override
    protected void postRequest(@NonNull final BillingRequest billingRequest) {
        requests.add(billingRequest);
        HANDLER.removeCallbacks(handleNextRequest);
        HANDLER.post(handleNextRequest);
    }
}
