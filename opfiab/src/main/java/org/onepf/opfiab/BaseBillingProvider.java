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
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import org.onepf.opfiab.model.billing.ConsumableDetails;
import org.onepf.opfiab.model.billing.EntitlementDetails;
import org.onepf.opfiab.model.billing.Inventory;
import org.onepf.opfiab.model.billing.Purchase;
import org.onepf.opfiab.model.billing.SkuDetails;
import org.onepf.opfiab.model.billing.SkusDetails;
import org.onepf.opfiab.model.billing.SubscriptionDetails;
import org.onepf.opfiab.model.event.ActivityResultEvent;
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
import org.onepf.opfiab.verification.PurchaseVerifier;
import org.onepf.opfutils.Checkable;
import org.onepf.opfutils.OPFChecks;
import org.onepf.opfutils.OPFPreferences;
import org.onepf.opfutils.OPFUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.greenrobot.event.EventBus;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

public abstract class BaseBillingProvider implements BillingProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(BaseBillingProvider.class);

    private static final String KEY_REQUEST = "request";
    private static final OPFPreferences PREFERENCES = new OPFPreferences(OPFIab.getContext());
    //TODO fail quietly?
    private static final Checkable CHECK_REQUEST = new Checkable() {
        @Override
        public boolean check() {
            return getPendingRequest() == null;
        }
    };
    @Nullable
    private static Request pendingRequest;

    @Nullable
    public static Request getPendingRequest() {
        if (pendingRequest == null && PREFERENCES.contains(KEY_REQUEST)) {
            pendingRequest = BillingEvent.fromJson(PREFERENCES.getString(KEY_REQUEST, ""),
                                                   Request.class);
        }
        return pendingRequest;
    }

    public static void setPendingRequest(@Nullable final Request pendingRequest) {
        BaseBillingProvider.pendingRequest = pendingRequest;
        if (pendingRequest == null) {
            PREFERENCES.remove(KEY_REQUEST);
        } else {
            PREFERENCES.put(KEY_REQUEST, pendingRequest.toJson());
        }
    }


    @NonNull
    protected final Context context = OPFIab.getContext();
    @NonNull
    protected final PurchaseVerifier purchaseVerifier;
    @NonNull
    protected final SkuResolver skuResolver;

    @NonNull
    private final EventBus eventBus = OPFIab.getEventBus();
    @NonNull
    private final String name = getName();
    @Nullable
    private final String packageName = getPackageName();

    protected BaseBillingProvider(
            @NonNull final PurchaseVerifier purchaseVerifier,
            @NonNull final SkuResolver skuResolver) {
        this.purchaseVerifier = purchaseVerifier;
        this.skuResolver = skuResolver;
    }

    public final void onEventAsync(@NonNull final Request event) {
        setPendingRequest(event);
        handleRequest(event);
    }

    public final void onEventAsync(@NonNull final ActivityResultEvent event) {
        final Activity activity = event.getActivity();
        final int requestCode = event.getRequestCode();
        final int resultCode = event.getResultCode();
        final Intent data = event.getData();
        onActivityResult(activity, requestCode, resultCode, data);
    }

    @SuppressFBWarnings({"BC_UNCONFIRMED_CAST", "DLS_DEAD_LOCAL_STORE"})
    @SuppressWarnings("ConstantConditions")
    protected final void handleRequest(@NonNull final Request request) {
        switch (request.getType()) {
            case CONSUME:
                final ConsumeRequest consumeRequest = (ConsumeRequest) request;
                consume(consumeRequest.getConsumableDetails());
                break;
            case PURCHASE:
                final PurchaseRequest purchaseRequest = (PurchaseRequest) request;
                final Activity activity = purchaseRequest.getActivity();
                final SkuDetails skuDetails = purchaseRequest.getSkuDetails();
                purchase(activity, skuDetails);
            case SKU_DETAILS:
                final SkuDetailsRequest skuDetailsRequest = (SkuDetailsRequest) request;
                skuDetails(skuDetailsRequest.getSkus());
                break;
            case INVENTORY:
                inventory();
                break;
        }
    }

    protected final void postResponse(@NonNull final Response response) {
        setPendingRequest(null);
        eventBus.post(response);
    }

    protected void postResponse(@NonNull final Response.Status status) {
        final Response response;
        OPFChecks.checkInit(CHECK_REQUEST, false);
        final Request request = getPendingRequest();
        //noinspection ConstantConditions
        final BillingEvent.Type type = request.getType();
        switch (type) {
            case CONSUME:
                final ConsumeRequest consumeRequest = (ConsumeRequest) request;
                response = new ConsumeResponse(consumeRequest, status, null);
                break;
            case PURCHASE:
                final PurchaseRequest purchaseRequest = (PurchaseRequest) request;
                response = new PurchaseResponse(purchaseRequest, status, null);
                break;
            case SKU_DETAILS:
                final SkuDetailsRequest skuDetailsRequest = (SkuDetailsRequest) request;
                response = new SkuDetailsResponse(skuDetailsRequest, status, null);
                break;
            case INVENTORY:
                final InventoryRequest inventoryRequest = (InventoryRequest) request;
                response = new InventoryResponse(inventoryRequest, status, null);
                break;
            default:
                throw new IllegalStateException();
        }
        postResponse(response);
    }

    protected void postResponse(@NonNull final Response.Status status,
                                @NonNull final SkusDetails skusDetails) {
        OPFChecks.checkInit(CHECK_REQUEST, false);
        final SkuDetailsRequest skuDetailsRequest = (SkuDetailsRequest) getPendingRequest();
        //noinspection ConstantConditions
        postResponse(new SkuDetailsResponse(skuDetailsRequest, status, skusDetails));
    }

    protected void postResponse(@NonNull final Response.Status status,
                                @NonNull final Inventory inventory) {
        OPFChecks.checkInit(CHECK_REQUEST, false);
        final InventoryRequest inventoryRequest = (InventoryRequest) getPendingRequest();
        //noinspection ConstantConditions
        postResponse(new InventoryResponse(inventoryRequest, status, inventory));
    }

    protected void postResponse(@NonNull final Response.Status status,
                                @NonNull final Purchase purchase) {
        OPFChecks.checkInit(CHECK_REQUEST, false);
        final PurchaseRequest purchaseRequest = (PurchaseRequest) getPendingRequest();
        //noinspection ConstantConditions
        postResponse(new PurchaseResponse(purchaseRequest, status, purchase));
    }

    protected void postResponse(@NonNull final Response.Status status,
                                @NonNull final ConsumableDetails consumableDetails) {
        OPFChecks.checkInit(CHECK_REQUEST, false);
        final ConsumeRequest consumeRequest = (ConsumeRequest) getPendingRequest();
        //noinspection ConstantConditions
        postResponse(new ConsumeResponse(consumeRequest, status, consumableDetails));
    }

    @SuppressFBWarnings("BC_UNCONFIRMED_CAST")
    @Override
    public void purchase(@NonNull final Activity activity, @NonNull final SkuDetails skuDetails) {
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
    }

    public void purchase(@NonNull final Activity activity,
                         @NonNull final ConsumableDetails skuDetails) { }


    public void purchase(@NonNull final Activity activity,
                         @NonNull final EntitlementDetails skuDetails) { }

    public void purchase(@NonNull final Activity activity,
                         @NonNull final SubscriptionDetails skuDetails) { }

    @Override
    public void onActivityResult(@NonNull final Activity activity, final int requestCode,
                                 final int resultCode, @Nullable final Intent data) { }

    @Override
    public boolean isAvailable() {
        if (TextUtils.isEmpty(packageName)) {
            throw new UnsupportedOperationException(
                    "You must override this method for packageless Billing Providers.");
        }
        return OPFUtils.isInstalled(context, packageName);
    }

    @Nullable
    @Override
    public Intent getStorePageIntent() {
        return null;
    }

    @Nullable
    @Override
    public Intent getRateIntent() {
        return null;
    }

    //CHECKSTYLE:OFF
    @SuppressWarnings("RedundantIfStatement")
    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof BaseBillingProvider)) return false;

        final BaseBillingProvider that = (BaseBillingProvider) o;

        if (!name.equals(that.name)) return false;
        if (packageName != null ? !packageName.equals(
                that.packageName) : that.packageName != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + (packageName != null ? packageName.hashCode() : 0);
        return result;
    }
    //CHECKSTYLE:ON

    public abstract static class Builder {

        @NonNull
        protected PurchaseVerifier purchaseVerifier = PurchaseVerifier.STUB;

        @NonNull
        protected SkuResolver skuResolver = SkuResolver.STUB;

        protected Builder purchaseVerifier(@NonNull final PurchaseVerifier purchaseVerifier) {
            this.purchaseVerifier = purchaseVerifier;
            return this;
        }

        protected Builder skuResolver(@NonNull final SkuResolver skuResolver) {
            this.skuResolver = skuResolver;
            return this;
        }

        public abstract BaseBillingProvider build();
    }
}
