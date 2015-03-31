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

package org.onepf.opfiab.api;

import android.support.annotation.NonNull;

import org.onepf.opfiab.billing.BillingProvider;
import org.onepf.opfiab.model.billing.Purchase;
import org.onepf.opfiab.model.billing.SkuDetails;
import org.onepf.opfiab.model.event.billing.ConsumeRequest;
import org.onepf.opfiab.model.event.billing.InventoryRequest;
import org.onepf.opfiab.model.event.billing.InventoryResponse;
import org.onepf.opfiab.model.event.billing.PurchaseRequest;
import org.onepf.opfiab.model.event.billing.PurchaseResponse;
import org.onepf.opfiab.model.event.billing.SkuDetailsRequest;
import org.onepf.opfiab.model.event.billing.SkuDetailsResponse;

import java.util.Set;

/**
 * Helper object to interact with {@link BillingProvider}.
 */
public interface IabHelper {

    /**
     * Sends {@link PurchaseRequest} to current {@link BillingProvider}.
     *
     * @param sku Stock Keeping Unit - unique product ID to purchase.
     * @see PurchaseResponse
     * @see Purchase
     */
    void purchase(@NonNull final String sku);

    /**
     * Sends {@link ConsumeRequest} to current {@link BillingProvider}.
     *
     * @param purchase Purchase object previously retrieved from {@link PurchaseResponse} or {@link InventoryResponse}.
     * @see Purchase
     */
    void consume(@NonNull final Purchase purchase);

    /**
     * Sends {@link InventoryRequest} to current {@link BillingProvider}.
     * <p/>
     * For the sake of performance, large inventory might not be loaded with one request.
     * Maximum number of items queried per request depends on {@link BillingProvider} implementation.
     *
     * @param startOver Flag indicating weather library should load inventory from the start,
     *                  or continue from the point of last successful request.
     * @see InventoryResponse
     * @see InventoryResponse#hasMore()
     */
    void inventory(final boolean startOver);

    /**
     * Sends {@link SkuDetailsRequest} to current {@link BillingProvider}.
     *
     * @param skus Stock Keeping Units - unique product IDs to query details for.
     * @see SkuDetailsResponse
     * @see SkuDetails
     */
    void skuDetails(@NonNull final Set<String> skus);

    /**
     * Same as {@link #skuDetails(java.util.Set)}.
     */
    void skuDetails(@NonNull final String... skus);
}
