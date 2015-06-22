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
import android.content.ActivityNotFoundException;
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
import org.onepf.opfiab.model.event.billing.BillingRequest;
import org.onepf.opfiab.model.event.billing.Status;
import org.onepf.opfiab.samsung.model.ItemType;
import org.onepf.opfiab.samsung.model.SamsungPurchase;
import org.onepf.opfiab.util.SyncedReference;
import org.onepf.opfiab.verification.PurchaseVerifier;
import org.onepf.opfutils.OPFChecks;
import org.onepf.opfutils.OPFLog;
import org.onepf.opfutils.OPFPreferences;
import org.onepf.opfutils.OPFUtils;

import java.util.Arrays;
import java.util.Collection;
import java.util.Set;

import static android.Manifest.permission.GET_ACCOUNTS;
import static android.Manifest.permission.INTERNET;
import static org.onepf.opfiab.model.event.billing.BillingEventType.INVENTORY;
import static org.onepf.opfiab.model.event.billing.BillingEventType.PURCHASE;
import static org.onepf.opfiab.model.event.billing.Status.ITEM_UNAVAILABLE;
import static org.onepf.opfiab.model.event.billing.Status.SUCCESS;
import static org.onepf.opfiab.model.event.billing.Status.UNAUTHORISED;
import static org.onepf.opfiab.model.event.billing.Status.UNKNOWN_ERROR;
import static org.onepf.opfiab.verification.PurchaseVerifier.DEFAULT;

public class SamsungBillingProvider
        extends ActivityBillingProvider<SamsungSkuResolver, PurchaseVerifier> {

    public static final String NAME = "Samsung";
    protected static final String PACKAGE = "com.sec.android.app.samsungapps";
    protected static final String INSTALLER = PACKAGE;
    protected static final String SAMSUNG_BILLING = "com.sec.android.iap.permission.BILLING";
    protected static final String KEY_ITEM_TYPE = NAME + ".item_type";

    protected static final int REQUEST_CODE_ACCOUNT = REQUEST_CODE;
    protected static final int REQUEST_CODE_PURCHASE = REQUEST_CODE + 1;

    protected static final long ACCOUNT_TIMEOUT = 5000;

    protected final OPFPreferences preferences = new OPFPreferences(context);
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

    @Override
    protected boolean needsActivity(@NonNull final BillingRequest billingRequest) {
        return Arrays.asList(PURCHASE, INVENTORY).contains(billingRequest.getType());
    }

    @NonNull
    @Override
    protected Collection<Integer> getRequestCodes() {
        return Arrays.asList(REQUEST_CODE_ACCOUNT, REQUEST_CODE_PURCHASE);
    }

    @Override
    protected void skuDetails(final Activity activity, @NonNull final Set<String> skus) {
        final Status initError = SamsungUtils.handleError(context, helper.init());
        if (initError != null) {
            postSkuDetailsResponse(initError, null);
            return;
        }

        final Bundle bundle = helper.getItemList(skuResolver.getGroupId());
        final Status error = SamsungUtils.handleError(context, bundle);
        if (error != null) {
            postSkuDetailsResponse(error, null);
            return;
        }

        final Collection<SkuDetails> skusDetails = SamsungUtils.getSkusDetails(bundle, skus);
        postSkuDetailsResponse(skusDetails == null ? UNKNOWN_ERROR : SUCCESS, skusDetails);
    }

    @Override
    protected void consume(final Activity activity, @NonNull final Purchase purchase) {
        // Samsung doesn't support consume http://developer.samsung.com/forum/thread/a/201/244297
        postConsumeResponse(SUCCESS, purchase);
    }

    protected boolean checkAuthorisation(@NonNull final Activity activity) {
        if (!SamsungUtils.hasSamsungAccount(context)) {
            return false;
        }

        final Intent intent = SamsungUtils.getAccountIntent();
        final SyncedReference<Boolean> syncAuthorisationResult = new SyncedReference<>();
        try {
            this.syncAuthorisationResult = syncAuthorisationResult;
            activity.startActivityForResult(intent, REQUEST_CODE_ACCOUNT);
            final Boolean result = syncAuthorisationResult.get(ACCOUNT_TIMEOUT);
            OPFLog.d("Samsung authorisation result: " + result);
            return result != null && result;
        } catch (ActivityNotFoundException exception) {
            OPFLog.e("Can't start Samsung authentication activity.", exception);
        } finally {
            this.syncAuthorisationResult = null;
        }
        return false;
    }

    @Override
    protected void inventory(final Activity activity, final boolean startOver) {
        final Status initError = SamsungUtils.handleError(context, helper.init());
        if (initError != null) {
            postInventoryResponse(initError, null, false);
            return;
        }

        final boolean authorized = checkAuthorisation(activity);
        releaseActivity(activity);
        if (!authorized) {
            postInventoryResponse(UNAUTHORISED, null, false);
            return;
        }

        final Bundle bundle = helper.getItemsInbox(skuResolver.getGroupId());
        final Status error = SamsungUtils.handleError(context, bundle);
        if (error != null) {
            postInventoryResponse(error, null, false);
            return;
        }

        //TODO check if consumables should be loaded
        final Collection<Purchase> purchases = SamsungUtils.getPurchasedItems(bundle, false);
        postInventoryResponse(purchases == null ? UNKNOWN_ERROR : SUCCESS, purchases, false);
    }

    @Override
    protected void purchase(@NonNull final Activity activity,
                            @NonNull final String sku) {
        final Status initError = SamsungUtils.handleError(context, helper.init());
        if (initError != null) {
            postPurchaseResponse(initError, null);
            return;
        }

        final String groupId = skuResolver.getGroupId();
        final Bundle bundle = helper.getItemList(groupId);
        final ItemType itemType = SamsungUtils.getItemType(bundle, sku);
        if (itemType == null) {
            postPurchaseResponse(ITEM_UNAVAILABLE, null);
            return;
        }

        if (!checkAuthorisation(activity)) {
            releaseActivity(activity);
            postPurchaseResponse(UNAUTHORISED, null);
            return;
        }

        preferences.put(KEY_ITEM_TYPE, itemType.name());
        final Intent purchaseIntent = SamsungUtils.getPurchaseIntent(context, groupId, sku);
        try {
            activity.startActivityForResult(purchaseIntent, REQUEST_CODE_PURCHASE);
            return;
        } catch (ActivityNotFoundException exception) {
            OPFLog.e("Can't start Samsung purchase activity.", exception);
        }

        postPurchaseResponse(UNKNOWN_ERROR, null);
    }

    @Override
    protected void onActivityResultSync(@NonNull final Activity activity,
                                        final int requestCode,
                                        final int resultCode,
                                        @Nullable final Intent data) {
        super.onActivityResultSync(activity, requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_ACCOUNT) {
            final SyncedReference<Boolean> syncAuthorisationResult = this.syncAuthorisationResult;
            if (syncAuthorisationResult != null) {
                syncAuthorisationResult.set(resultCode == Activity.RESULT_OK);
            }
        }
    }

    @SuppressWarnings("PMD.NPathComplexity")
    @Override
    protected void onActivityResult(@NonNull final Activity activity,
                                    final int requestCode,
                                    final int resultCode,
                                    @Nullable final Intent data) {
        if (requestCode != REQUEST_CODE_PURCHASE) {
            return;
        }

        if (resultCode != Activity.RESULT_OK) {
            postPurchaseResponse(Status.USER_CANCELED, null);
        } else {
            final SamsungPurchase samsungPurchase;
            final Bundle bundle = data == null ? null : data.getExtras();
            final Status error = SamsungUtils.handleError(context, bundle);
            if (error != null) {
                postPurchaseResponse(error, null);
            } else if (data == null
                    || (samsungPurchase = SamsungUtils.getPurchase(bundle)) == null) {
                postPurchaseResponse(UNKNOWN_ERROR, null);
                OPFLog.e("Purchase data is null");
            } else {
                final ItemType type = preferences.contains(KEY_ITEM_TYPE)
                        ? ItemType.valueOf(preferences.getString(KEY_ITEM_TYPE))
                        : null;
                preferences.remove(KEY_ITEM_TYPE);
                final Purchase purchase = type != null
                        ? SamsungUtils.convertPurchase(samsungPurchase, type)
                        : null;
                postPurchaseResponse(purchase != null ? SUCCESS : UNKNOWN_ERROR, purchase);
            }
        }

        releaseActivity(activity);
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
