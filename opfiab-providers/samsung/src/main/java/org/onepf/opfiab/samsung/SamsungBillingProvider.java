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

import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.onepf.opfiab.billing.ActivityBillingProvider;
import org.onepf.opfiab.billing.BaseBillingProvider;
import org.onepf.opfiab.model.BillingProviderInfo;
import org.onepf.opfiab.model.billing.Purchase;
import org.onepf.opfiab.model.billing.SkuDetails;
import org.onepf.opfiab.model.event.billing.Status;
import org.onepf.opfiab.samsung.model.ItemType;
import org.onepf.opfiab.samsung.model.SamsungPurchase;
import org.onepf.opfiab.samsung.model.SamsungPurchasedItem;
import org.onepf.opfiab.samsung.model.SamsungSkuDetails;
import org.onepf.opfiab.verification.PurchaseVerifier;
import org.onepf.opfutils.OPFChecks;
import org.onepf.opfutils.OPFLog;
import org.onepf.opfutils.OPFPreferences;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Set;

import static org.onepf.opfiab.model.event.billing.Status.SUCCESS;
import static org.onepf.opfiab.model.event.billing.Status.UNKNOWN_ERROR;
import static org.onepf.opfiab.verification.PurchaseVerifier.DEFAULT;

public class SamsungBillingProvider
        extends ActivityBillingProvider<SamsungSkuResolver, PurchaseVerifier> {

    protected static final String NAME = "Samsung";
    protected static final String PACKAGE = "com.sec.android.app.samsungapps";
    public static final BillingProviderInfo INFO = new BillingProviderInfo(NAME, PACKAGE);
    protected static final String PERMISSION_BILLING = "com.sec.android.iap.permission.BILLING";
    protected static final String KEY_SKU = "sku";
    protected static final String KEY_ITEM_TYPE = "item_type";

    protected static final int REQUEST_CODE_ACCOUNT = REQUEST_CODE;
    protected static final int REQUEST_CODE_PURCHASE = REQUEST_CODE + 1;


    protected final OPFPreferences preferences = new OPFPreferences(context);
    @NonNull
    protected final SamsungBillingHelper helper;

    protected SamsungBillingProvider(@NonNull final Context context,
                                     @NonNull final SamsungSkuResolver skuResolver,
                                     @NonNull final PurchaseVerifier purchaseVerifier,
                                     @NonNull final BillingMode billingMode) {
        super(context, skuResolver, purchaseVerifier);
        this.helper = new SamsungBillingHelper(context, billingMode);
    }

    @Nullable
    public ItemType resolveItemType(@NonNull final String sku) {
        // Ridiculously inefficient...
        final Bundle bundle = helper.getItemList(skuResolver.getGroupId());
        final Status error = SamsungUtils.getStatusForError(context, bundle);
        if (error != null) {
            return null;
        }

        final Collection<SamsungSkuDetails> items = SamsungUtils.getSkusDetails(bundle);
        if (items != null) {
            for (final SamsungSkuDetails item : items) {
                if (sku.equals(item.getItemId())) {
                    return item.getItemType();
                }
            }
        }
        return null;
    }

    @NonNull
    @Override
    public BillingProviderInfo getInfo() {
        return INFO;
    }

    @Override
    public void checkManifest() {
        OPFChecks.checkPermission(context, Manifest.permission.INTERNET);
        OPFChecks.checkPermission(context, PERMISSION_BILLING);
    }

    @NonNull
    @Override
    protected Collection<Integer> getRequestCodes() {
        return Arrays.asList(REQUEST_CODE_ACCOUNT, REQUEST_CODE_PURCHASE);
    }

    @Override
    protected void purchase(@NonNull final Activity activity,
                            @NonNull final String sku) {
        final Bundle bundle = helper.init();
        final Status error = SamsungUtils.getStatusForError(context, bundle);
        if (error != null) {
            postPurchaseResponse(error, null);
            return;
        }

        final Intent intent = SamsungUtils.getAccountIntent();
        preferences.put(KEY_SKU, sku);
        try {
            activity.startActivityForResult(intent, REQUEST_CODE_ACCOUNT);
            return;
        } catch (ActivityNotFoundException exception) {
            OPFLog.e("Can't start Samsung authentication activity.", exception);
        }

        postPurchaseResponse(UNKNOWN_ERROR, null);
    }

    @Override
    protected void skuDetails(final Activity activity, @NonNull final Set<String> skus) {
        final Bundle bundle = helper.getItemList(skuResolver.getGroupId());
        final Status error = SamsungUtils.getStatusForError(context, bundle);
        if (error != null) {
            postSkuDetailsResponse(error, null);
            return;
        }

        final Collection<SamsungSkuDetails> items = SamsungUtils.getSkusDetails(bundle);
        if (items != null) {
            final Collection<String> unloadedItems = new ArrayList<>(skus);
            final Collection<SkuDetails> skusDetails = new ArrayList<>(items.size());
            for (final SamsungSkuDetails item : items) {
                final String sku = item.getItemId();
                if (unloadedItems.contains(sku)) {
                    final SkuDetails skuDetails = SamsungUtils.convertSkuDetails(INFO, item);
                    skusDetails.add(skuDetails);
                    unloadedItems.remove(sku);
                }
            }
            for (final String sku : unloadedItems) {
                skusDetails.add(new SkuDetails(sku));
            }
            postSkuDetailsResponse(SUCCESS, skusDetails);
            return;
        }

        postSkuDetailsResponse(UNKNOWN_ERROR, null);
    }

    @Override
    protected void inventory(final Activity activity, final boolean startOver) {
        final Bundle bundle = helper.getItemsInbox(skuResolver.getGroupId());
        final Status error = SamsungUtils.getStatusForError(context, bundle);
        if (error != null) {
            postInventoryResponse(error, null, false);
            return;
        }

        final Collection<SamsungPurchasedItem> items = SamsungUtils.getPurchasedItems(bundle);
        if (items != null) {
            final Collection<Purchase> purchases = new ArrayList<>();
            for (final SamsungPurchasedItem item : items) {
                //TODO check if consumables should be loaded
                if (item.getItemType() != ItemType.CONSUMABLE) {
                    purchases.add(SamsungUtils.convertPurchasedItems(INFO, item));
                }
            }
            postInventoryResponse(SUCCESS, purchases, false);
            return;
        }

        postInventoryResponse(UNKNOWN_ERROR, null, false);
    }

    @Override
    protected void consume(final Activity activity, @NonNull final Purchase purchase) {
        // Samsung doesn't support consume http://developer.samsung.com/forum/thread/a/201/244297
        postConsumeResponse(SUCCESS, purchase);
    }

    @Override
    protected void onActivityResult(@NonNull final Activity activity,
                                    final int requestCode,
                                    final int resultCode,
                                    @Nullable final Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            postPurchaseResponse(Status.USER_CANCELED, null);
        } else {
            switch (requestCode) {
                case REQUEST_CODE_ACCOUNT:
                    final String groupId = skuResolver.getGroupId();
                    final String sku = preferences.getString(KEY_SKU);
                    if (sku == null) {
                        break;
                    }
                    final ItemType itemType = resolveItemType(sku);
                    if (itemType == null) {
                        postPurchaseResponse(Status.ITEM_UNAVAILABLE, null);
                        OPFLog.e("Failed to resolve purchaseType: " + sku);
                        break;
                    }
                    preferences.put(KEY_ITEM_TYPE, itemType.name());
                    preferences.remove(KEY_SKU);
                    final Intent intent = SamsungUtils.getPurchaseIntent(context, groupId, sku);
                    activity.startActivityForResult(intent, REQUEST_CODE_PURCHASE);
                    return;
                case REQUEST_CODE_PURCHASE:
                    final SamsungPurchase samsungPurchase;
                    final Bundle bundle = data == null ? null : data.getExtras();
                    final Status error = SamsungUtils.getStatusForError(context, bundle);
                    if (error != null) {
                        postPurchaseResponse(error, null);
                    } else if (data == null ||
                            (samsungPurchase = SamsungUtils.getPurchase(bundle)) == null) {
                        postPurchaseResponse(UNKNOWN_ERROR, null);
                        OPFLog.e("Purchase data is null");
                    } else {
                        final ItemType type = preferences.contains(KEY_ITEM_TYPE)
                                ? ItemType.valueOf(preferences.getString(KEY_ITEM_TYPE))
                                : null;
                        final Purchase purchase = type != null
                                ? SamsungUtils.convertPurchase(INFO, samsungPurchase, type)
                                : null;
                        postPurchaseResponse(purchase != null ? SUCCESS : UNKNOWN_ERROR, purchase);
                    }
                    break;
            }
        }
    }

    public static class Builder
            extends BaseBillingProvider.Builder<SamsungSkuResolver, PurchaseVerifier> {

        @NonNull
        private BillingMode billingMode = BillingMode.PRODUCTION;

        public Builder(@NonNull final Context context) {
            super(context, null, DEFAULT);
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

        @Override
        public Builder setSkuResolver(
                @NonNull final SamsungSkuResolver skuResolver) {
            return (Builder) super.setSkuResolver(skuResolver);
        }

        @Override
        public Builder setPurchaseVerifier(
                @NonNull final PurchaseVerifier purchaseVerifier) {
            return (Builder) super.setPurchaseVerifier(purchaseVerifier);
        }
    }
}
