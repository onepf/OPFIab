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

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;

import org.onepf.opfiab.BillingProvider;
import org.onepf.opfiab.model.billing.ConsumableDetails;
import org.onepf.opfiab.model.billing.EntitlementDetails;
import org.onepf.opfiab.model.billing.SkuDetails;
import org.onepf.opfiab.model.billing.SubscriptionDetails;
import org.onepf.opfiab.model.event.ActivityResultEvent;
import org.onepf.opfiab.model.event.request.ConsumeRequest;
import org.onepf.opfiab.model.event.request.InventoryRequest;
import org.onepf.opfiab.model.event.request.PurchaseRequest;
import org.onepf.opfiab.model.event.request.Request;
import org.onepf.opfiab.model.event.request.SkuDetailsRequest;
import org.onepf.opfiab.sku.SkuResolver;
import org.onepf.opfiab.verification.PurchaseVerifier;

public abstract class BaseBillingProvider implements BillingProvider {

    @NonNull
    private final PurchaseVerifier purchaseVerifier;

    @NonNull
    private final SkuResolver skuResolver;

    protected BaseBillingProvider(
            @NonNull final PurchaseVerifier purchaseVerifier,
            @NonNull final SkuResolver skuResolver) {
        this.purchaseVerifier = purchaseVerifier;
        this.skuResolver = skuResolver;
    }

    public final void onEventAsync(@NonNull final Request event) {
        handleRequest(event);
    }

    public final void onEvent(@NonNull final ActivityResultEvent event) {
        final Activity activity = event.getActivity();
        final int requestCode = event.getRequestCode();
        final int resultCode = event.getResultCode();
        final Intent data = event.getData();
        onActivityResult(activity, requestCode, resultCode, data);
    }

    private void handleRequest(@NonNull final Request request) {
        switch (request.getType()) {
            case CONSUME:
                final ConsumeRequest consumeRequest = (ConsumeRequest) request;
                consume(consumeRequest.getConsumableDetails());
                break;
            case PURCHASE:
                final PurchaseRequest purchaseRequest = (PurchaseRequest) request;
                final Activity activity = purchaseRequest.getActivity();
                final SkuDetails skuDetails = purchaseRequest.getSkuDetails();
                switch (skuDetails.getType()) {
                    case CONSUMABLE:
                        purchase(activity, (ConsumableDetails) skuDetails);
                        break;
                    case ENTITLEMENT:
                        purchase(activity, (EntitlementDetails) skuDetails);
                        break;
                    case SUBSCRIPTION:
                        purchase(activity, (SubscriptionDetails) skuDetails);
                        break;
                }
            case SKU_DETAILS:
                final SkuDetailsRequest skuDetailsRequest = (SkuDetailsRequest) request;
                skuDetails(skuDetailsRequest.getSkus());
                break;
            case INVENTORY:
                @SuppressWarnings("UnusedDeclaration")
                final InventoryRequest inventoryRequest = (InventoryRequest) request;
                inventory();
                break;
        }
    }


    public abstract static class Builder {

        @NonNull
        protected PurchaseVerifier purchaseVerifier = PurchaseVerifier.STUB;

        @NonNull
        protected SkuResolver skuResolver = SkuResolver.STUB;

        public <T extends PurchaseVerifier> Builder purchaseVerifier(@NonNull final T purchaseVerifier) {
            this.purchaseVerifier = purchaseVerifier;
            return this;
        }

        public Builder skuResolver(@NonNull final SkuResolver skuResolver) {
            this.skuResolver = skuResolver;
            return this;
        }

        public abstract BaseBillingProvider build();
    }
}
