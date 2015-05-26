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

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import org.onepf.opfiab.billing.ActivityBillingProvider;
import org.onepf.opfiab.billing.BaseBillingProvider;
import org.onepf.opfiab.model.BillingProviderInfo;
import org.onepf.opfiab.model.billing.Purchase;
import org.onepf.opfiab.verification.PurchaseVerifier;
import org.onepf.opfutils.OPFChecks;

import java.util.Collection;
import java.util.Set;

public class SamsungBillingProvider
        extends ActivityBillingProvider<SamsungSkuResolver, PurchaseVerifier> {

    protected static final String NAME = "Samsung";
    protected static final String PACKAGE = "com.sec.android.app.samsungapps";
    public static final BillingProviderInfo INFO = new BillingProviderInfo(NAME, PACKAGE);
    protected static final String PERMISSION_BILLING = "com.sec.android.iap.permission.BILLING";
    protected static final int REQUST_CODE_PURCHASE = REQUEST_CODE;
    protected static final int REQUST_CODE_ACCOUNT = REQUEST_CODE + 1;
    @NonNull
    private final SamsungBillingHelper helper;

    protected SamsungBillingProvider(@NonNull final Context context,
                                     @NonNull final SamsungSkuResolver skuResolver,
                                     @NonNull final PurchaseVerifier purchaseVerifier,
                                     @NonNull final BillingMode billingMode) {
        super(context, skuResolver, purchaseVerifier);
        this.helper = new SamsungBillingHelper(context, billingMode);
    }

    @NonNull
    @Override
    public BillingProviderInfo getInfo() {
        return null;
    }

    @Override
    public void checkManifest() {
        OPFChecks.checkPermission(context, Manifest.permission.INTERNET);
        OPFChecks.checkPermission(context, PERMISSION_BILLING);
    }

    @NonNull
    @Override
    protected Collection<Integer> getRequestCodes() {
        return super.getRequestCodes();
    }

    @Override
    protected void purchase(@NonNull final Activity activity,
                            @NonNull final String sku) {

    }

    @Override
    protected void skuDetails(@NonNull final Set<String> skus) {

    }

    @Override
    protected void inventory(final boolean startOver) {

    }

    @Override
    protected void consume(@NonNull final Purchase purchase) {

    }

    @Override
    protected void onActivityResult(@NonNull final Activity activity,
                                    final int requestCode,
                                    final int resultCode,
                                    @NonNull final Intent data) {
        switch (requestCode) {
            case REQUST_CODE_ACCOUNT:
                break;
            case REQUST_CODE_PURCHASE:
                break;
        }
    }

    public static class Builder
            extends BaseBillingProvider.Builder<SamsungSkuResolver, PurchaseVerifier> {

        @NonNull
        private BillingMode billingMode = BillingMode.PRODUCTION;

        public Builder(@NonNull final Context context) {
            super(context, null, PurchaseVerifier.DEFAULT);
        }

        @NonNull
        public Builder setBillingMode(@NonNull final BillingMode billingMode) {
            this.billingMode = billingMode;
            return this;
        }

        @Override
        public SamsungBillingProvider build() {
            if (skuResolver == null) {
                throw new IllegalStateException("SamsungSkuResolver must be set.");
            }
            return null;
        }
    }
}
