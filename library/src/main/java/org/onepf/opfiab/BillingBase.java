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
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.onepf.opfiab.model.billing.ConsumableDetails;
import org.onepf.opfiab.model.billing.EntitlementDetails;
import org.onepf.opfiab.model.billing.SubscriptionDetails;

import java.util.Collection;

interface BillingBase {

    void purchase(@NonNull final Activity activity,
                  @NonNull final ConsumableDetails consumableDetails);

    void purchase(@NonNull final Activity activity,
                  @NonNull final SubscriptionDetails subscriptionDetails);

    void purchase(@NonNull final Activity activity,
                  @NonNull final EntitlementDetails entitlementDetails);

    void consume(@NonNull final ConsumableDetails consumableDetails);

    void inventory();

    void skuInfo(@NonNull final Collection<String> sku);

    void onActivityResult(final int requestCode, final int resultCode, @Nullable final Intent data);
}
