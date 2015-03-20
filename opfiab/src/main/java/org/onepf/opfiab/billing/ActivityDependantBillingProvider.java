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

package org.onepf.opfiab.billing;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.onepf.opfiab.OPFIabActivity;
import org.onepf.opfiab.model.event.billing.BillingRequest;
import org.onepf.opfiab.model.event.billing.PurchaseRequest;
import org.onepf.opfiab.sku.SkuResolver;
import org.onepf.opfiab.verification.PurchaseVerifier;

public abstract class ActivityDependantBillingProvider<R extends SkuResolver, V extends PurchaseVerifier>
        extends BaseBillingProvider<R, V> {

    protected ActivityDependantBillingProvider(@NonNull final Context context,
                                               @NonNull final R skuResolver,
                                               @NonNull final V purchaseVerifier,
                                               @Nullable final Integer requestCode) {
        super(context, skuResolver, purchaseVerifier, requestCode);
    }

    protected ActivityDependantBillingProvider(@NonNull final Context context,
                                               @NonNull final R skuResolver,
                                               @NonNull final V purchaseVerifier) {
        super(context, skuResolver, purchaseVerifier);
    }

    @Override
    protected abstract void purchase(
            @SuppressWarnings("NullableProblems") @NonNull final Activity activity,
            @NonNull final String sku);

    @Override
    public void onEventAsync(@NonNull final BillingRequest billingRequest) {
        final PurchaseRequest purchaseRequest;
        if (billingRequest.getType() == BillingRequest.Type.PURCHASE
                && (purchaseRequest = (PurchaseRequest) billingRequest).needsFakeActivity()) {
            // We have to start OPFIabActivity to properly handle this request
            OPFIabActivity.start(purchaseRequest.getActivity(), purchaseRequest);
            return;
        }
        super.onEventAsync(billingRequest);
    }
}
