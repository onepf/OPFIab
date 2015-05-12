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

package org.onepf.opfiab.samsung;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;

import com.sec.android.iap.lib.helper.SamsungIapHelper;

import org.onepf.opfiab.billing.ActivityBillingProvider;
import org.onepf.opfiab.model.BillingProviderInfo;
import org.onepf.opfiab.model.billing.Purchase;
import org.onepf.opfiab.sku.SkuResolver;
import org.onepf.opfiab.verification.PurchaseVerifier;

import java.util.Set;

public class SamsungBillingProvider extends ActivityBillingProvider<SkuResolver, PurchaseVerifier> {



    protected SamsungBillingProvider(@NonNull final Context context,
                                     @NonNull final SkuResolver skuResolver,
                                     @NonNull final PurchaseVerifier purchaseVerifier) {
        super(context, skuResolver, purchaseVerifier);
    }

    @Override
    protected void onActivityResult(final Activity activity, final int i, final int i1,
                                    final Intent intent) {
    }

    @Override
    protected void purchase(final Activity activity, final String s) {

    }

    @Override
    protected void skuDetails(final Set set) {

    }

    @Override
    protected void inventory(final boolean b) {

    }

    @Override
    protected void consume(final Purchase purchase) {

    }

    @NonNull
    @Override
    public BillingProviderInfo getInfo() {
        return null;
    }

    @Override
    public void checkManifest() {

    }
}
