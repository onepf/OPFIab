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

package org.onepf.opfiab.opfiab_uitest.mock;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.onepf.opfiab.OPFIab;
import org.onepf.opfiab.model.event.RequestHandledEvent;
import org.onepf.opfiab.model.event.billing.BillingRequest;
import org.onepf.opfiab.model.event.billing.PurchaseResponse;
import org.onepf.opfiab.verification.VerificationResult;

import static org.onepf.opfiab.model.event.billing.Status.SUCCESS;

/**
 * @author antonpp
 * @since 14.05.15
 */
public class MockOkBillingProvider extends MockBillingProvider {

    private static final String NAME = MockOkBillingProvider.class.getSimpleName();

    @NonNull
    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public void checkManifest() {
        // nothing
    }

    @Override
    public boolean isAvailable() {
        sleep();
        return true;
    }

    @Override
    public void onBillingRequest(@NonNull BillingRequest billingRequest) {
        OPFIab.post(new RequestHandledEvent(billingRequest));
        sleep();
        OPFIab.post(new PurchaseResponse(SUCCESS, getName(), null, VerificationResult.SUCCESS));
    }

    @Nullable
    @Override
    public Intent getStorePageIntent() {
        return null;
    }

    @Nullable
    @Override
    public Intent getRateIntent() {
        return null;
    }
}
