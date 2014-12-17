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

package org.onepf.opfiab.google;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.onepf.opfiab.billing.BaseBillingProvider;
import org.onepf.opfiab.billing.connection.BillingProviderConnection;
import org.onepf.opfiab.model.BillingProviderInfo;
import org.onepf.opfiab.model.billing.ConsumableDetails;
import org.onepf.opfiab.model.billing.EntitlementDetails;
import org.onepf.opfiab.model.billing.SubscriptionDetails;
import org.onepf.opfiab.sku.SkuResolver;
import org.onepf.opfiab.verification.PurchaseVerifier;

import java.util.Collection;

public class GoogleBillingProvider extends BaseBillingProvider {

    public GoogleBillingProvider(
            @NonNull final PurchaseVerifier purchaseVerifier,
            @NonNull final SkuResolver skuResolver) {
        super(purchaseVerifier, skuResolver);
    }

    @NonNull
    @Override
    public BillingProviderInfo getInfo() {
        return null;
    }

    @NonNull
    @Override
    public BillingProviderConnection getConnection() {
        return null;
    }

    @Override
    public void purchase(@NonNull final Activity activity,
                         @NonNull final ConsumableDetails consumableDetails) {

    }

    @Override
    public void purchase(@NonNull final Activity activity,
                         @NonNull final SubscriptionDetails subscriptionDetails) {

    }

    @Override
    public void purchase(@NonNull final Activity activity,
                         @NonNull final EntitlementDetails entitlementDetails) {

    }

    @Override
    public void consume(@NonNull final ConsumableDetails consumableDetails) {

    }

    @Override
    public void inventory() {

    }

    @Override
    public void skuDetails(@NonNull final Collection<String> skus) {

    }

    @Override
    public void onActivityResult(@NonNull final Activity activity, final int requestCode,
                                 final int resultCode, @Nullable final Intent data) {

    }


    public static class Builder extends BaseBillingProvider.Builder {

        @Override
        public GoogleBillingProvider build() {
            return new GoogleBillingProvider(purchaseVerifier, skuResolver);
        }

        public <T extends GooglePurchaseVerifier> BaseBillingProvider.Builder purchaseVerifier(
                @NonNull final T purchaseVerifier) {
            return super.purchaseVerifier(purchaseVerifier);
        }
    }
}
