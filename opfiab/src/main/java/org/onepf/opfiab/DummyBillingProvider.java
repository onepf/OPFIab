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

import android.app.Activity;
import android.support.annotation.NonNull;

import org.onepf.opfiab.billing.BillingController;
import org.onepf.opfiab.model.BillingProviderInfo;
import org.onepf.opfiab.model.billing.ConsumableDetails;
import org.onepf.opfiab.model.billing.SkuDetails;
import org.onepf.opfiab.model.event.request.Request;

import java.util.Collection;

import static org.onepf.opfiab.model.event.response.Response.Status.BILLING_UNAVAILABLE;

@SuppressWarnings("ConstantConditions")
final class DummyBillingProvider extends BaseBillingProvider {

    public DummyBillingProvider() {
        super(null, null);
    }

    @NonNull
    @Override
    public BillingProviderInfo getInfo() {
        return null;
    }

    @NonNull
    @Override
    public BillingController getController() {
        return null;
    }

    @Override
    protected void handleRequest(@NonNull final Request request) {
        super.handleRequest(request);
        postResponse(BILLING_UNAVAILABLE);
    }

    @Override
    public void purchase(@NonNull final Activity activity, @NonNull final SkuDetails skuDetails) { }

    @Override
    public void consume(@NonNull final ConsumableDetails consumableDetails) { }

    @Override
    public void inventory() { }

    @Override
    public void skuDetails(@NonNull final Collection<String> skus) { }
}
