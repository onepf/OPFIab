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
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.onepf.opfiab.model.billing.Purchase;
import org.onepf.opfiab.model.event.billing.BillingRequest;

import java.util.Set;

class IabHelperAdapter extends IabHelper {

    @NonNull
    protected final IabHelper iabHelper;

    IabHelperAdapter(@NonNull final IabHelper iabHelper) {
        super();
        this.iabHelper = iabHelper;
    }

    @Override
    protected void postRequest(@NonNull final BillingRequest billingRequest) {
        iabHelper.postRequest(billingRequest);
    }

    @Override
    protected BillingRequest newPurchaseRequest(@NonNull final Activity activity,
                                                @NonNull final String sku) {
        return iabHelper.newPurchaseRequest(activity, sku);
    }

    @Override
    protected BillingRequest newConsumeRequest(@NonNull final Purchase purchase) {
        return iabHelper.newConsumeRequest(purchase);
    }

    @Override
    protected BillingRequest newInventoryRequest(final boolean startOver) {
        return iabHelper.newInventoryRequest(startOver);
    }

    @Override
    protected BillingRequest newSkuDetailsRequest(@NonNull final Set<String> skus) {
        return iabHelper.newSkuDetailsRequest(skus);
    }

    @Override
    public void onActivityResult(@NonNull final Activity activity,
                                 final int requestCode, final int resultCode,
                                 @Nullable final Intent data) {
        iabHelper.onActivityResult(activity, requestCode, resultCode, data);
    }
}
