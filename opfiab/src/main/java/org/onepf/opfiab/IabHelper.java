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

import org.onepf.opfiab.model.billing.Purchase;
import org.onepf.opfiab.model.event.billing.BillingRequest;
import org.onepf.opfiab.model.event.billing.ConsumeRequest;
import org.onepf.opfiab.model.event.billing.InventoryRequest;
import org.onepf.opfiab.model.event.billing.PurchaseRequest;
import org.onepf.opfiab.model.event.billing.SkuDetailsRequest;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class IabHelper {

    protected final BillingBase billingBase = OPFIab.getBase();

    IabHelper() {
        super();
    }

    protected void postRequest(@NonNull final BillingRequest billingRequest) {
        billingBase.postRequest(billingRequest);
    }

    public void purchase(@NonNull final String sku) {
        postRequest(new PurchaseRequest(sku));
    }

    public void consume(@NonNull final Purchase purchase) {
        postRequest(new ConsumeRequest(purchase));
    }

    public void inventory(final boolean startOver) {
        postRequest(new InventoryRequest(startOver));
    }

    public void skuDetails(@NonNull final Set<String> skus) {
        postRequest(new SkuDetailsRequest(skus));
    }

    public final void skuDetails(@NonNull final String... skus) {
        skuDetails(new HashSet<>(Arrays.asList(skus)));
    }
}
