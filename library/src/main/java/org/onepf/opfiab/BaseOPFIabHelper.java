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
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.onepf.opfiab.broadcast.OPFIabGlobalReceiver;
import org.onepf.opfiab.model.Consumable;
import org.onepf.opfiab.model.Options;
import org.onepf.opfiab.model.Subscription;

class BaseOPFIabHelper implements OPFIabHelper, StandaloneOPFIabHelper {

    @NonNull
    final Context context;

    @NonNull
    final Options options;

    @NonNull
    final BillingListenerCompositor billingListenerCompositor;

    BaseOPFIabHelper(@NonNull final Context context, @NonNull final Options options) {
        this.context = context.getApplicationContext();
        this.options = options;
        this.billingListenerCompositor = new BillingListenerCompositor(
                options.getBillingListener());

        OPFIabBroadcast.register(new OPFIabGlobalReceiver(billingListenerCompositor));
    }


    @Override
    public void purchase(@NonNull final Activity activity, @NonNull final Consumable consumable) {

    }

    @Override
    public void consume(@Nullable final Activity activity, @NonNull final Consumable consumable) {

    }

    @Override
    public void subscribe(@Nullable final Activity activity,
                          @NonNull final Subscription subscription) {

    }

    @Override
    public void inventory(@Nullable final Activity activity) {

    }

    @Override
    public void skuInfo(@Nullable final Activity activity, @NonNull final String sku) {

    }

    @Override
    public void purchase(@NonNull final Consumable consumable) {

    }

    @Override
    public void consume(@NonNull final Consumable consumable) {

    }

    @Override
    public void subscribe(@NonNull final Subscription subscription) {

    }

    @Override
    public void inventory() {

    }

    @Override
    public void skuInfo(@NonNull final String sku) {

    }
}
