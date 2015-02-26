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

package org.onepf.opfiab.model.event.billing;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.onepf.opfiab.model.BillingProviderInfo;
import org.onepf.opfiab.model.billing.Purchase;

import java.util.Collections;
import java.util.List;

public class InventoryResponse extends BillingResponse {

    @Nullable
    private final List<Purchase> inventory;
    private final boolean hasMore;

    public InventoryResponse(@Nullable final BillingProviderInfo providerInfo,
                             @NonNull final Status status,
                             @Nullable final List<Purchase> inventory,
                             final boolean hasMore) {
        super(providerInfo, Type.INVENTORY, status);
        this.inventory = inventory == null ? null : Collections.unmodifiableList(inventory);
        this.hasMore = hasMore;
    }

    @Nullable
    public List<Purchase> getInventory() {
        return inventory;
    }

    public boolean hasMore() {
        return hasMore;
    }
}
