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

package org.onepf.opfiab.billing;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.onepf.opfiab.OPFIab;
import org.onepf.opfiab.model.BillingProviderInfo;
import org.onepf.opfiab.model.event.RequestHandledEvent;
import org.onepf.opfiab.model.event.billing.BillingRequest;
import org.onepf.opfiab.model.event.billing.BillingResponse;

/**
 * This interface represents billing service provider, capable of handling in-app purchases.
 * <p/>
 * All methods of this class should be suitable for calling from <b>single</b> background thread.
 */
public interface BillingProvider {

    /**
     * Retrieve information about this BillingProvider.
     *
     * @return single immutable object containing all relevant information about this provider.
     */
    @NonNull
    BillingProviderInfo getInfo();

    /**
     * Check if Manifest contains all necessary entries.
     *
     * @throws java.lang.IllegalStateException if manifest doesn't contain all necessary entries.
     */
    void checkManifest();

    /**
     * Indicates whether this provider is available on the system.
     * <br>
     * Called before each request, thus it might be a good idea to cache intermediate result.
     *
     * @return true is BillingProvider is currently available, false otherwise.
     */
    boolean isAvailable();

    /**
     * Indicates whether user is current logged in with this provider.
     *
     * @return <b>false</b> if user is <b>not</b> authorised, true otherwise.
     */
    boolean isAuthorised();

    /**
     * Entry point for all billing requests.
     * <br>
     * Called from <b>single</b> background thread.
     * <p/>
     * As soon as billing request is handled, BillingProvider <b>must</b> notify library with
     * {@link RequestHandledEvent} using {@link OPFIab#post(Object)}. Same method should be used
     * with proper {@link BillingResponse} object when result of performed action becomes available.
     *
     * @param billingRequest Billing request to handle with this billing provider.
     */
    void onEventAsync(@NonNull final BillingRequest billingRequest);

    /**
     * Acquire {@link Intent} to open representation of this App within this BillingProvider.
     *
     * @return Intent object suitable for {@link Activity#startActivity(Intent)}. Can be null.
     */
    @Nullable
    Intent getStorePageIntent();

    /**
     * Acquire {@link Intent} to rate this App within this BillingProvider.
     *
     * @return Intent object suitable for {@link Activity#startActivity(Intent)}. Can be null.
     */
    @Nullable
    Intent getRateIntent();
}
