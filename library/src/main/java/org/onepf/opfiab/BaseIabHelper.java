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
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.onepf.opfiab.model.Configuration;
import org.onepf.opfiab.model.billing.ConsumableDetails;
import org.onepf.opfiab.model.billing.EntitlementDetails;
import org.onepf.opfiab.model.billing.SkuDetails;
import org.onepf.opfiab.model.billing.SubscriptionDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

class BaseIabHelper extends IabHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(BaseIabHelper.class);

    @NonNull
    private final Context context;

    @NonNull
    private final Configuration configuration;

    @NonNull
    private final GlobalBillingListener globalBillingListener;

    BaseIabHelper(@NonNull final Context context, @NonNull final Configuration configuration) {
        this.context = context.getApplicationContext();
        this.configuration = configuration;
        this.globalBillingListener = new GlobalBillingListener(
                configuration.getBillingListener());
    }

    protected void purchase(@NonNull final Activity activity,
                            @NonNull final SkuDetails skuDetails) {

    }

    @Override
    public final void purchase(@NonNull final Activity activity,
                               @NonNull final ConsumableDetails consumableDetails) {
        purchase(activity, (SkuDetails) consumableDetails);
    }

    @Override
    public final void purchase(@NonNull final Activity activity,
                               @NonNull final SubscriptionDetails subscriptionDetails) {
        purchase(activity, (SkuDetails) subscriptionDetails);
    }

    @Override
    public final void purchase(@NonNull final Activity activity,
                               @NonNull final EntitlementDetails entitlementDetails) {
        purchase(activity, (SkuDetails) entitlementDetails);
    }

    @Override
    public void consume(@NonNull final ConsumableDetails consumableDetails) {

    }

    @Override
    public void inventory() {

    }

    @Override
    public void skuInfo(@NonNull final Collection<String> sku) {

    }

    @Override
    public void onActivityResult(final int requestCode,
                                 final int resultCode,
                                 @Nullable final Intent data) {

    }
}
