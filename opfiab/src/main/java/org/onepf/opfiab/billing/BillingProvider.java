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

package org.onepf.opfiab.billing;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.onepf.opfiab.model.BillingProviderInfo;
import org.onepf.opfiab.model.event.ActivityResultEvent;
import org.onepf.opfiab.model.event.billing.BillingRequest;

public interface BillingProvider {

    @NonNull
    BillingProviderInfo getInfo();

    /**
     * Indicates whether this provider is available on the system.
     * Called before each request, thus it might be a good idea to cache intermediate result.
     *
     * @return true is BillingProvider is currently available, false otherwise.
     */
    boolean isAvailable();

    boolean isAuthorised();

    void onEventAsync(@NonNull final BillingRequest billingRequest);

    void onEventAsync(@NonNull final ActivityResultEvent activityResultEvent);

    @Nullable
    Intent getStorePageIntent();

    @Nullable
    Intent getRateIntent();
}
