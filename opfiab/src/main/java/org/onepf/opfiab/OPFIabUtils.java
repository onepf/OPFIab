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

package org.onepf.opfiab;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.onepf.opfiab.model.BillingProviderInfo;
import org.onepf.opfiab.model.event.BillingEvent;
import org.onepf.opfiab.model.event.request.ConsumeRequest;
import org.onepf.opfiab.model.event.request.InventoryRequest;
import org.onepf.opfiab.model.event.request.PurchaseRequest;
import org.onepf.opfiab.model.event.request.Request;
import org.onepf.opfiab.model.event.request.SkuDetailsRequest;
import org.onepf.opfiab.model.event.response.ConsumeResponse;
import org.onepf.opfiab.model.event.response.InventoryResponse;
import org.onepf.opfiab.model.event.response.PurchaseResponse;
import org.onepf.opfiab.model.event.response.Response;
import org.onepf.opfiab.model.event.response.SkuDetailsResponse;
import org.onepf.opfiab.sku.SkuResolver;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

public final class OPFIabUtils {

    private OPFIabUtils() {
        throw new UnsupportedOperationException();
    }

    @SuppressFBWarnings({"BC_UNCONFIRMED_CAST"})
    public static Response emptyResponse(@Nullable final BillingProviderInfo providerInfo,
                                         @NonNull final Request request,
                                         @NonNull final Response.Status status) {
        final Response response;
        final BillingEvent.Type type = request.getType();
        switch (type) {
            case CONSUME:
                final ConsumeRequest consumeRequest = (ConsumeRequest) request;
                response = new ConsumeResponse(providerInfo, consumeRequest, status, null);
                break;
            case PURCHASE:
                final PurchaseRequest purchaseRequest = (PurchaseRequest) request;
                response = new PurchaseResponse(providerInfo, purchaseRequest, status, null);
                break;
            case SKU_DETAILS:
                final SkuDetailsRequest skuDetailsRequest = (SkuDetailsRequest) request;
                response = new SkuDetailsResponse(providerInfo, skuDetailsRequest, status, null);
                break;
            case INVENTORY:
                final InventoryRequest inventoryRequest = (InventoryRequest) request;
                response = new InventoryResponse(providerInfo, inventoryRequest, status, null);
                break;
            default:
                throw new IllegalStateException();
        }
        return response;
    }

    public static Set<String> resolve(@NonNull final SkuResolver resolver,
                                      @NonNull final String... skus) {
        return resolveSkus(resolver, Arrays.asList(skus));
    }

    public static Set<String> resolveSkus(@NonNull final SkuResolver resolver,
                                          @NonNull final Collection<String> skus) {
        final Set<String> resolvedSkus = new HashSet<>();
        for (final String sku : skus) {
            resolvedSkus.add(resolver.resolve(sku));
        }
        return resolvedSkus;
    }
}
