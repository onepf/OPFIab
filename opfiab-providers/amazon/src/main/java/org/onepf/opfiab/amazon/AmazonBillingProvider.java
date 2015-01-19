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

package org.onepf.opfiab.amazon;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.onepf.opfiab.BaseBillingProvider;
import org.onepf.opfiab.billing.BillingController;
import org.onepf.opfiab.model.billing.ConsumableDetails;
import org.onepf.opfiab.model.billing.SkuDetails;
import org.onepf.opfiab.sku.SkuResolver;
import org.onepf.opfiab.verification.PurchaseVerifier;

import java.util.Collection;

public class AmazonBillingProvider extends BaseBillingProvider {

    @NonNull
    private static final String NAME = "Amazon";

    @NonNull
    private static final String PACKAGE_NAME = "com.amazon.venezia";


    private final AmazonBillingController controller = new AmazonBillingController();

    protected AmazonBillingProvider(
            @NonNull final PurchaseVerifier purchaseVerifier,
            @NonNull final SkuResolver skuResolver) {
        super(purchaseVerifier, skuResolver);
    }

    @NonNull
    @Override
    public String getName() {
        return NAME;
    }

    @Nullable
    @Override
    public String getPackageName() {
        return PACKAGE_NAME;
    }

    @NonNull
    @Override
    public BillingController getController() {
        return controller;
    }

    @Override
    public void purchase(@NonNull final Activity activity, @NonNull final SkuDetails skuDetails) {

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
}
