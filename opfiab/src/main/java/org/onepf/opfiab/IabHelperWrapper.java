/*
 * Copyright 2012-2014 One Platform Foundation
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
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.onepf.opfiab.model.billing.ConsumableDetails;
import org.onepf.opfiab.model.billing.EntitlementDetails;
import org.onepf.opfiab.model.billing.SubscriptionDetails;

import java.util.Collection;

class IabHelperWrapper extends IabHelper {

    @NonNull
    protected final IabHelper iabHelper;

    public IabHelperWrapper(@NonNull final IabHelper iabHelper) {
        this.iabHelper = iabHelper;
    }

    @Override
    public void purchase(@NonNull final Activity activity,
                         @NonNull final ConsumableDetails consumableDetails) {
        iabHelper.purchase(activity, consumableDetails);
    }

    @Override
    public void purchase(@NonNull final Activity activity,
                         @NonNull final SubscriptionDetails subscriptionDetails) {
        iabHelper.purchase(activity, subscriptionDetails);
    }

    @Override
    public void purchase(@NonNull final Activity activity,
                         @NonNull final EntitlementDetails entitlementDetails) {
        iabHelper.purchase(activity, entitlementDetails);
    }

    @Override
    public void consume(@NonNull final ConsumableDetails consumableDetails) {
        iabHelper.consume(consumableDetails);
    }

    @Override
    public void inventory() {
        iabHelper.inventory();
    }

    @Override
    public void skuDetails(@NonNull final Collection<String> skus) {
        iabHelper.skuDetails(skus);
    }

    @Override
    public void onActivityResult(@NonNull final Activity activity, final int requestCode,
                                 final int resultCode,
                                 @Nullable final Intent data) {
        iabHelper.onActivityResult(activity, requestCode, resultCode, data);
    }
}
