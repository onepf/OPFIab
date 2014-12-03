/*
 * Copyright 2012-2014 One Platform Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
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

import org.onepf.opfiab.model.billing.InAppDetails;
import org.onepf.opfiab.model.Options;
import org.onepf.opfiab.model.billing.SubscriptionDetails;

import java.util.Collection;

class BaseIabHelper extends IabHelper {

    @NonNull
    private final Context context;

    @NonNull
    private final Options options;

    @NonNull
    private final GlobalBillingListener globalBillingListener;

    BaseIabHelper(@NonNull final Context context, @NonNull final Options options) {
        this.context = context.getApplicationContext();
        this.options = options;
        this.globalBillingListener = new GlobalBillingListener(
                options.getBillingListener());

    }

    void unsubscribe(@NonNull final ManagedIabHelper managedOPFIabHelper) {
        globalBillingListener.unsubscribe(managedOPFIabHelper);
    }

    void subscribe(@NonNull final ManagedIabHelper managedOPFIabHelper) {
        globalBillingListener.subscribe(managedOPFIabHelper);
    }

    @Override
    public void purchase(@NonNull final Activity activity, @NonNull final InAppDetails inAppDetails) {

    }

    @Override
    public void purchase(@Nullable final Activity activity,
                         @NonNull final SubscriptionDetails subscriptionDetails) {

    }

    @Override
    public void consume(@NonNull final InAppDetails inAppDetails) {

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
