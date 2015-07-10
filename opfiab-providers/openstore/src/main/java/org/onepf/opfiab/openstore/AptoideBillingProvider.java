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

package org.onepf.opfiab.openstore;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.onepf.opfiab.billing.BaseBillingProvider;
import org.onepf.opfiab.sku.TypedSkuResolver;
import org.onepf.opfiab.verification.PurchaseVerifier;

public class AptoideBillingProvider extends OpenStoreBillingProvider {

    public static final String NAME = "APTOIDE";
    protected static final String[] PACKAGES = new String[]{"cm.aptoide.pt"};


    public AptoideBillingProvider(@NonNull final Context context,
                                  @NonNull final TypedSkuResolver skuResolver,
                                  @NonNull final PurchaseVerifier purchaseVerifier,
                                  @Nullable final OpenStoreIntentMaker intentMaker) {
        super(context, skuResolver, purchaseVerifier, intentMaker);
    }

    public static class Builder extends OpenStoreBillingProvider.OpenStoreBuilder<Builder> {

        public Builder(@NonNull final Context context) {
            super(context);
            setIntentMaker(OpenStoreUtils.getIntentMaker(NAME, PACKAGES));
        }

        @Override
        public BaseBillingProvider build() {
            if (skuResolver == null) {
                throw new IllegalStateException();
            }
            return new AptoideBillingProvider(context, skuResolver,
                    purchaseVerifier == null ? PurchaseVerifier.DEFAULT : purchaseVerifier,
                    intentMaker);
        }
    }
}
