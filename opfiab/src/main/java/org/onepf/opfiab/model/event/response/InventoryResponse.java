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

package org.onepf.opfiab.model.event.response;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.onepf.opfiab.model.BillingProviderInfo;
import org.onepf.opfiab.model.billing.Purchase;
import org.onepf.opfiab.model.event.request.InventoryRequest;
import org.onepf.opfiab.model.event.request.Request;

import java.util.Collection;
import java.util.Collections;

public class InventoryResponse extends Response {

    @Nullable
    private final Collection<Purchase> inventory;

    public InventoryResponse(@Nullable final BillingProviderInfo providerInfo,
                             @NonNull final Request request,
                             @NonNull final Status status,
                             @Nullable final Collection<Purchase> inventory) {
        super(providerInfo, Type.INVENTORY, request, status);
        this.inventory = inventory == null ? null : Collections.unmodifiableCollection(inventory);
    }

    @Nullable
    public Collection<Purchase> getInventory() {
        return inventory;
    }

    @NonNull
    @Override
    public InventoryRequest getRequest() {
        return (InventoryRequest) super.getRequest();
    }
}
