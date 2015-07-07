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

package org.onepf.opfiab.yandex;

import android.content.Context;
import android.support.annotation.NonNull;

import org.onepf.opfiab.billing.BaseBillingProvider;
import org.onepf.opfiab.billing.BaseBillingProviderBuilder;
import org.onepf.opfiab.openstore.OpenStoreBillingHelper;
import org.onepf.opfiab.openstore.OpenStoreBillingProvider;
import org.onepf.opfiab.sku.TypedSkuResolver;
import org.onepf.opfiab.verification.PurchaseVerifier;

public class YandexBillingProvider extends OpenStoreBillingProvider {

    protected YandexBillingProvider(@NonNull final Context context,
                                    @NonNull final TypedSkuResolver skuResolver,
                                    @NonNull final PurchaseVerifier purchaseVerifier) {
        super(context, skuResolver, purchaseVerifier);
    }

    @Override
    public String toString() {
        return "Yandex";
    }

    @NonNull
    @Override
    protected OpenStoreBillingHelper getHelper() {
        return new YandexBillingHelper(context);
    }

    public static class Builder extends BaseBillingProviderBuilder<Builder, TypedSkuResolver,
            PurchaseVerifier> {

        public Builder(@NonNull final Context context) {
            super(context);
        }

        @Override
        public BaseBillingProvider build() {
            if (skuResolver == null) {
                throw new IllegalStateException("SkuResolver must be set.");
            }
            return new YandexBillingProvider(context, skuResolver,
                    purchaseVerifier == null ? PurchaseVerifier.DEFAULT : purchaseVerifier);
        }
    }
}
