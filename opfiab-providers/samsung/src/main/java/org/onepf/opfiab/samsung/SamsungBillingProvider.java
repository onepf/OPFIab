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
import android.content.IntentSender;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.json.JSONException;
import org.onepf.opfiab.billing.BaseBillingProvider;
import org.onepf.opfiab.billing.BaseBillingProviderBuilder;
import org.onepf.opfiab.billing.Compatibility;
import org.onepf.opfiab.model.billing.Purchase;
import org.onepf.opfiab.model.billing.SkuDetails;
import org.onepf.opfiab.model.billing.SkuType;
import org.onepf.opfiab.model.event.android.ActivityResult;
import org.onepf.opfiab.model.event.billing.BillingEventType;
import org.onepf.opfiab.model.event.billing.BillingRequest;
import org.onepf.opfiab.model.event.billing.BillingResponse;
import org.onepf.opfiab.model.event.billing.ConsumeRequest;
import org.onepf.opfiab.model.event.billing.ConsumeResponse;
import org.onepf.opfiab.model.event.billing.InventoryRequest;
import org.onepf.opfiab.model.event.billing.InventoryResponse;
import org.onepf.opfiab.model.event.billing.PurchaseRequest;
import org.onepf.opfiab.model.event.billing.PurchaseResponse;
import org.onepf.opfiab.model.event.billing.SkuDetailsRequest;
import org.onepf.opfiab.model.event.billing.SkuDetailsResponse;
import org.onepf.opfiab.model.event.billing.Status;
import org.onepf.opfiab.samsung.model.SamsungPurchase;
import org.onepf.opfiab.util.ActivityForResultLauncher;
import org.onepf.opfiab.util.SyncedReference;
import org.onepf.opfiab.verification.PurchaseVerifier;
import org.onepf.opfiab.verification.VerificationResult;
import org.onepf.opfutils.OPFChecks;
import org.onepf.opfutils.OPFLog;
import org.onepf.opfutils.OPFPreferences;
import org.onepf.opfutils.OPFUtils;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import static android.Manifest.permission.GET_ACCOUNTS;
import static android.Manifest.permission.INTERNET;
import static org.onepf.opfiab.model.event.billing.Status.SERVICE_UNAVAILABLE;
import static org.onepf.opfiab.model.event.billing.Status.SUCCESS;
import static org.onepf.opfiab.model.event.billing.Status.UNAUTHORISED;
import static org.onepf.opfiab.model.event.billing.Status.UNKNOWN_ERROR;
import static org.onepf.opfiab.model.event.billing.Status.USER_CANCELED;
import static org.onepf.opfiab.verification.PurchaseVerifier.DEFAULT;
import static org.onepf.opfiab.verification.VerificationResult.ERROR;

@SuppressWarnings({"PMD.NPathComplexity", "PMD.ModifiedCyclomaticComplexity", "PMD.GodClass", "PMD.StdCyclomaticComplexity"})
public class SamsungBillingProvider extends BaseBillingProvider<SamsungSkuResolver,
        PurchaseVerifier> {

    public static final String NAME = "Samsung";
    protected static final String PACKAGE = "com.sec.android.app.samsungapps";
    protected static final String INSTALLER = PACKAGE;
    protected static final String SAMSUNG_BILLING = "com.sec.android.iap.permission.BILLING";

    protected static final long ACCOUNT_TIMEOUT = 5000;
    protected static final int BATCH_SIZE = 15;
    protected static final String KEY_LAST_ITEM = NAME + ".last_item";


    protected final OPFPreferences preferences = new OPFPreferences(context);
    protected final OPFPreferences consumablePurchases = new OPFPreferences(context, NAME);
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
        if (!OPFUtils.isInstalled(context, PACKAGE)) {
            return Compatibility.INCOMPATIBLE;
        }
        if (INSTALLER.equals(OPFUtils.getPackageInstaller(context))) {
            return Compatibility.PREFERRED;
        }
        return Compatibility.COMPATIBLE;
    }

    @Override
    protected BillingResponse verify(@NonNull final BillingResponse response) {
        final BillingResponse verifiedResponse = super.verify(response);
        if (verifiedResponse.getStatus() != SUCCESS) {
            return verifiedResponse;
        }
        // Due to API limitations this BillingProvider doesn't return consumables in inventory
        // requests. However this there's a possibility of error during purchase verification
        // process user might not get a verified consumable purchase in onPurchase() callback.
        // To work around this issue we'll this kind of purchases in SharedPreferences.
        final BillingEventType type = verifiedResponse.getType();
        if (type == BillingEventType.PURCHASE) {
            final PurchaseResponse purchaseResponse = (PurchaseResponse) verifiedResponse;
            final Purchase purchase = purchaseResponse.getPurchase();
            if (purchaseResponse.getVerificationResult() == ERROR && purchase != null
                    && purchase.getType() == SkuType.CONSUMABLE) {
                final String token = purchase.getToken();
                final String originalJson = purchase.getOriginalJson();
                if (token != null && originalJson != null) {
                    consumablePurchases.put(token, originalJson);
                }
            }
        } else if (type == BillingEventType.INVENTORY) {
            final InventoryResponse inventoryResponse = (InventoryResponse) verifiedResponse;
            final Map<Purchase, VerificationResult> inventory = inventoryResponse.getInventory();
            for (final Map.Entry<Purchase, VerificationResult> entry : inventory.entrySet()) {
                final VerificationResult result = entry.getValue();
                final Purchase purchase = entry.getKey();
                final String token = purchase.getToken();
                if (token != null && result != ERROR && consumablePurchases.contains(token)) {
                    consumablePurchases.remove(token);
                }
            }
        }
        return verifiedResponse;
    }

    protected Status checkAuthorisation(@NonNull final BillingRequest billingRequest) {
        if (!SamsungUtils.hasSamsungAccount(context)) {
            return UNAUTHORISED;
        }
        final ActivityResult result = requestActivityResult(billingRequest,
                new ActivityForResultLauncher(DEFAULT_REQUEST_CODE) {
                    @Override
                    public void onStartForResult(@NonNull final Activity activity)
                            throws IntentSender.SendIntentException {
                        final Intent intent = SamsungUtils.getAccountIntent();
                        activity.startActivityForResult(intent, DEFAULT_REQUEST_CODE);
                    }
                });
        if (result != null && result.getResultCode() == Activity.RESULT_OK) {
            return null;
        }
        return OPFUtils.isConnected(context) ? UNAUTHORISED : SERVICE_UNAVAILABLE;
    }

    @Override
    protected void skuDetails(@NonNull final SkuDetailsRequest request) {
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
        final Status authStatus = checkAuthorisation(request);
        if (authStatus != null) {
            postEmptyResponse(request, authStatus);
            return;
        }

        final boolean startOver = request.startOver();
        final int start = startOver ? 1 : preferences.getInt(KEY_LAST_ITEM, 1);
        final int end = start + BATCH_SIZE - 1;
        final Bundle bundle = helper.getItemsInbox(skuResolver.getGroupId(), start, end);
        final Status error = SamsungUtils.handleError(context, bundle);
        if (error != null) {
            postEmptyResponse(request, error);
            return;
        }

        final Collection loadedItems = SamsungUtils.getItems(bundle);
        final int loadedCount = loadedItems == null ? 0 : loadedItems.size();
        if (loadedCount > 0) {
            preferences.put(KEY_LAST_ITEM, start + loadedCount);
        }

        //TODO check if consumables should be loaded
        final Collection<Purchase> purchases = SamsungUtils.getPurchasedItems(bundle, false);
        if (purchases != null) {
            // Add all consumables that might be stored in SharedPreferences.
            final Map<String, ?> all = consumablePurchases.getPreferences().getAll();
            for (final Map.Entry<String, ?> entry : all.entrySet()) {
                final String value = (String) entry.getValue();
                try {
                    final SamsungPurchase samsungPurchase = new SamsungPurchase(value);
                    final Purchase purchase = SamsungUtils
                            .convertPurchase(samsungPurchase, SkuType.CONSUMABLE);
                    purchases.add(purchase);
                } catch (JSONException exception) {
                    OPFLog.e("", exception);
                    consumablePurchases.remove(entry.getKey());
                }
            }
        }
        final Status status = purchases == null ? UNKNOWN_ERROR : SUCCESS;
        final boolean hasMore = loadedCount == BATCH_SIZE;
        postResponse(new InventoryResponse(status, getName(), purchases, hasMore));
    }

    @Override
    protected void purchase(@NonNull final PurchaseRequest request) {
        //TODO make sure init is not required
        //        final Status initError = SamsungUtils.handleError(context, helper.init());
        //        if (initError != null) {
        //            postEmptyResponse(request, initError);
        //            return;
        //        }

        final Status authStatus = checkAuthorisation(request);
        if (authStatus != null) {
            postEmptyResponse(request, authStatus);
            return;
        }

        final String sku = request.getSku();
        final ActivityResult result = requestActivityResult(request,
                new ActivityForResultLauncher(DEFAULT_REQUEST_CODE) {
                    @Override
                    public void onStartForResult(@NonNull final Activity activity)
                            throws IntentSender.SendIntentException {
                        final String groupId = skuResolver.getGroupId();
                        final Intent intent = SamsungUtils.getPurchaseIntent(context, groupId, sku);
                        activity.startActivityForResult(intent, DEFAULT_REQUEST_CODE);
                    }
                });
        if (result == null || result.getResultCode() != Activity.RESULT_OK) {
            postEmptyResponse(request, result == null ? UNKNOWN_ERROR : USER_CANCELED);
            return;
        }
        final Intent data = result.getData();
        final Bundle bundle = data == null ? null : data.getExtras();
        final Status error = SamsungUtils.handleError(context, bundle);
        final SamsungPurchase samsungPurchase = SamsungUtils.getPurchase(bundle);
        if (error != null || samsungPurchase == null) {
            postEmptyResponse(request, error != null ? error : UNKNOWN_ERROR);
            return;
        }
        final SkuType skuType = skuResolver.resolveType(sku);
        final Purchase purchase = SamsungUtils.convertPurchase(samsungPurchase, skuType);
        postResponse(new PurchaseResponse(SUCCESS, getName(), purchase));
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
