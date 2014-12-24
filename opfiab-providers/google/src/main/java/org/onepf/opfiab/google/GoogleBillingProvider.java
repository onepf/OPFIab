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

import org.onepf.opfiab.BaseBillingProvider;
import org.onepf.opfiab.billing.BillingProviderConnection;
import org.onepf.opfiab.model.BillingProviderInfo;
import org.onepf.opfiab.model.billing.ConsumableDetails;
import org.onepf.opfiab.model.billing.SkuDetails;
import org.onepf.opfiab.sku.SkuResolver;
import org.onepf.opfiab.verification.PublicKeyPurchaseVerifier;
import org.onepf.opfiab.verification.PurchaseVerifier;

import java.util.Collection;

public class GoogleBillingProvider extends BaseBillingProvider {

    @NonNull
    private static final String NAME = "Google";

    @NonNull
    private static final String PACKAGE_NAME = "com.google.play";


    @NonNull
    private final GoogleConnection connection = new GoogleConnection();

    @NonNull
    private final BillingProviderInfo info = new BillingProviderInfo(NAME, PACKAGE_NAME);

    protected GoogleBillingProvider(
            @NonNull final PurchaseVerifier purchaseVerifier,
            @NonNull final SkuResolver skuResolver) {
        super(purchaseVerifier, skuResolver);
    }

    @NonNull
    @Override
    public BillingProviderInfo getInfo() {
        return info;
    }

    @NonNull
    @Override
    public BillingProviderConnection getConnection() {
        return connection;
    }

    @Override
    public void purchase(@NonNull final Activity activity,
                         @NonNull final SkuDetails consumableDetails) {
    }

    @Override
    public void skuDetails(@NonNull final Collection<String> skus) {

    }

    @Override
    public void consume(@NonNull final ConsumableDetails consumableDetails) {

    }

    @Override
    public void inventory() {

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

        public Builder purchaseVerifier(
                @NonNull final PublicKeyPurchaseVerifier purchaseVerifier) {
            super.purchaseVerifier(purchaseVerifier);
            return this;
        }

        @Override
        public Builder skuResolver(@NonNull final SkuResolver skuResolver) {
            super.skuResolver(skuResolver);
            return this;
        }
    }
}
