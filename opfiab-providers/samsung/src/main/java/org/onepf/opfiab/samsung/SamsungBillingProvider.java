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

package org.onepf.opfiab.samsung;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.onepf.opfiab.billing.ActivityBillingProvider;
import org.onepf.opfiab.billing.BaseBillingProviderBuilder;
import org.onepf.opfiab.billing.Compatibility;
import org.onepf.opfiab.model.billing.Purchase;
import org.onepf.opfiab.model.billing.SkuDetails;
import org.onepf.opfiab.model.billing.SkuType;
import org.onepf.opfiab.model.event.android.ActivityResultEvent;
import org.onepf.opfiab.model.event.billing.BillingRequest;
import org.onepf.opfiab.model.event.billing.ConsumeRequest;
import org.onepf.opfiab.model.event.billing.ConsumeResponse;
import org.onepf.opfiab.model.event.billing.InventoryRequest;
import org.onepf.opfiab.model.event.billing.InventoryResponse;
import org.onepf.opfiab.model.event.billing.PurchaseRequest;
import org.onepf.opfiab.model.event.billing.PurchaseResponse;
import org.onepf.opfiab.model.event.billing.SkuDetailsRequest;
import org.onepf.opfiab.model.event.billing.SkuDetailsResponse;
import org.onepf.opfiab.model.event.billing.Status;
import org.onepf.opfiab.samsung.model.ItemType;
import org.onepf.opfiab.samsung.model.SamsungPurchase;
import org.onepf.opfiab.util.SyncedReference;
import org.onepf.opfiab.verification.PurchaseVerifier;
import org.onepf.opfutils.OPFChecks;
import org.onepf.opfutils.OPFUtils;

import java.util.Collection;
import java.util.Set;

import static android.Manifest.permission.GET_ACCOUNTS;
import static android.Manifest.permission.INTERNET;
import static org.onepf.opfiab.model.event.billing.Status.SUCCESS;
import static org.onepf.opfiab.model.event.billing.Status.UNAUTHORISED;
import static org.onepf.opfiab.model.event.billing.Status.UNKNOWN_ERROR;
import static org.onepf.opfiab.model.event.billing.Status.USER_CANCELED;
import static org.onepf.opfiab.verification.PurchaseVerifier.DEFAULT;

public class SamsungBillingProvider
        extends ActivityBillingProvider<SamsungSkuResolver, PurchaseVerifier> {

    public static final String NAME = "Samsung";
    protected static final String PACKAGE = "com.sec.android.app.samsungapps";
    protected static final String INSTALLER = PACKAGE;
    protected static final String SAMSUNG_BILLING = "com.sec.android.iap.permission.BILLING";

    protected static final long ACCOUNT_TIMEOUT = 5000;

    @NonNull
    protected final SamsungBillingHelper helper;
    @Nullable
    protected SyncedReference<Boolean> syncAuthorisationResult;

    protected SamsungBillingProvider(@NonNull final Context context,
                                     @NonNull final SamsungSkuResolver skuResolver,
                                     @NonNull final PurchaseVerifier purchaseVerifier,
                                     @NonNull final BillingMode billingMode) {
        super(context, skuResolver, purchaseVerifier);
        this.helper = new SamsungBillingHelper(context, billingMode);
    }

    @Override
    public void checkManifest() {
        OPFChecks.checkPermission(context, GET_ACCOUNTS);
        OPFChecks.checkPermission(context, INTERNET);
        OPFChecks.checkPermission(context, SAMSUNG_BILLING);
    }

    @NonNull
    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public boolean isAvailable() {
        return OPFUtils.isInstalled(context, PACKAGE);
    }

    @NonNull
    @Override
    public Compatibility checkCompatibility() {
        final Bundle bundle = helper.init();
        if (SamsungUtils.getResponse(bundle) != Response.ERROR_NONE) {
            return Compatibility.INCOMPATIBLE;
        }
        if (INSTALLER.equals(OPFUtils.getPackageInstaller(context))) {
            return Compatibility.PREFERRED;
        }
        return Compatibility.COMPATIBLE;
    }

    protected boolean checkAuthorisation(@NonNull final BillingRequest billingRequest) {
        if (!SamsungUtils.hasSamsungAccount(context)) {
            return false;
        }
        final ActivityResultEvent result = requestActivityResult(billingRequest,
                new ActivityResultHelper(DEFAULT_REQUEST_CODE) {
                    @Override
                    public void onStartForResult(@NonNull final Activity activity) {
                        final Intent intent = SamsungUtils.getAccountIntent();
                        activity.startActivityForResult(intent, DEFAULT_REQUEST_CODE);
                    }
                });
        return result != null && result.getResultCode() == Activity.RESULT_OK;
    }

    @Override
    protected void skuDetails(@NonNull final SkuDetailsRequest request) {
        final Status initError = SamsungUtils.handleError(context, helper.init());
        if (initError != null) {
            postEmptyResponse(request, initError);
            return;
        }

        final Bundle bundle = helper.getItemList(skuResolver.getGroupId());
        final Status error = SamsungUtils.handleError(context, bundle);
        if (error != null) {
            postEmptyResponse(request, error);
            return;
        }

        final Set<String> skus = request.getSkus();
        final Collection<SkuDetails> skusDetails = SamsungUtils.getSkusDetails(bundle, skus);
        final Status status = skusDetails == null ? UNKNOWN_ERROR : SUCCESS;
        postResponse(new SkuDetailsResponse(status, getName(), skusDetails));
    }

    @Override
    protected void inventory(@NonNull final InventoryRequest request) {
        final Status initError = SamsungUtils.handleError(context, helper.init());
        if (initError != null) {
            postEmptyResponse(request, initError);
            return;
        }

        final boolean authorized = checkAuthorisation(request);
        if (!authorized) {
            postEmptyResponse(request, UNAUTHORISED);
            return;
        }

        final Bundle bundle = helper.getItemsInbox(skuResolver.getGroupId());
        final Status error = SamsungUtils.handleError(context, bundle);
        if (error != null) {
            postEmptyResponse(request, error);
            return;
        }

        //TODO check if consumables should be loaded
        final Collection<Purchase> purchases = SamsungUtils.getPurchasedItems(bundle, false);
        final Status status = purchases == null ? UNKNOWN_ERROR : SUCCESS;
        postResponse(new InventoryResponse(status, getName(), purchases, false));
    }

    @Override
    protected void purchase(@NonNull final PurchaseRequest request) {
        final Status initError = SamsungUtils.handleError(context, helper.init());
        if (initError != null) {
            postEmptyResponse(request, initError);
            return;
        }

        if (!checkAuthorisation(request)) {
            postEmptyResponse(request, UNAUTHORISED);
            return;
        }

        final String sku = request.getSku();
        final ActivityResultEvent result = requestActivityResult(request,
                new ActivityResultHelper(DEFAULT_REQUEST_CODE) {
                    @Override
                    public void onStartForResult(@NonNull final Activity activity) {
                        final String groupId = skuResolver.getGroupId();
                        final Intent intent = SamsungUtils.getPurchaseIntent(context, groupId, sku);
                        activity.startActivityForResult(intent, DEFAULT_REQUEST_CODE);
                    }
                });
        if (result == null) {
            postEmptyResponse(request, UNKNOWN_ERROR);
        } else if (result.getResultCode() != Activity.RESULT_OK) {
            postEmptyResponse(request, USER_CANCELED);
        } else {
            final Intent data = result.getData();
            final SamsungPurchase samsungPurchase;
            final Bundle bundle = data == null ? null : data.getExtras();
            final Status error = SamsungUtils.handleError(context, bundle);
            if (error != null) {
                postEmptyResponse(request, error);
            } else if (data == null
                    || (samsungPurchase = SamsungUtils.getPurchase(bundle)) == null) {
                postEmptyResponse(request, UNKNOWN_ERROR);
            } else {
                final SkuType skuType = skuResolver.resolveType(sku);
                final ItemType itemType = ItemType.fromSkuType(skuType);
                final Purchase purchase = SamsungUtils.convertPurchase(samsungPurchase, itemType);
                postResponse(new PurchaseResponse(SUCCESS, getName(), purchase));
            }
        }
    }

    @Override
    protected void consume(@NonNull final ConsumeRequest request) {
        // Samsung doesn't support consume http://developer.samsung.com/forum/thread/a/201/244297
        postResponse(new ConsumeResponse(SUCCESS, getName(), request.getPurchase()));
    }

    public static class Builder extends BaseBillingProviderBuilder<Builder, SamsungSkuResolver,
            PurchaseVerifier> {

        @NonNull
        private BillingMode billingMode = BillingMode.PRODUCTION;

        public Builder(@NonNull final Context context) {
            super(context);
        }

        @NonNull
        public Builder setBillingMode(@NonNull final BillingMode billingMode) {
            this.billingMode = billingMode;
            return this;
        }

        @Override
        public SamsungBillingProvider build() {
            if (skuResolver == null) {
                throw new IllegalStateException("SamsungSkuResolver must be set.");
            }
            return new SamsungBillingProvider(context, skuResolver,
                    purchaseVerifier == null ? DEFAULT : purchaseVerifier,
                    billingMode);
        }
    }
}
