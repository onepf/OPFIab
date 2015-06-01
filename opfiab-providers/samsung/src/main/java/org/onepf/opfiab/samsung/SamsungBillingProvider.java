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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Set;

import static org.onepf.opfiab.verification.PurchaseVerifier.DEFAULT;

public class SamsungBillingProvider
        extends ActivityBillingProvider<SamsungSkuResolver, PurchaseVerifier> {

    protected static final String NAME = "Samsung";
    protected static final String PACKAGE = "com.sec.android.app.samsungapps";
    public static final BillingProviderInfo INFO = new BillingProviderInfo(NAME, PACKAGE);
    protected static final String PERMISSION_BILLING = "com.sec.android.iap.permission.BILLING";

    protected final int requestCodeAccount = DEFAULT_REQUEST_CODE;
    protected final int requestCodePurchase = DEFAULT_REQUEST_CODE + 1;
    @NonNull
    private final SamsungBillingHelper helper;

    private String pendingPurchaseSku;

    protected SamsungBillingProvider(@NonNull final Context context,
                                     @NonNull final SamsungSkuResolver skuResolver,
                                     @NonNull final PurchaseVerifier purchaseVerifier,
                                     @NonNull final BillingMode billingMode) {
        super(context, skuResolver, purchaseVerifier);
        this.helper = new SamsungBillingHelper(context, billingMode);
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
        return Arrays.asList(requestCodeAccount, requestCodePurchase);
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
        try {
            pendingPurchaseSku = sku;
            activity.startActivityForResult(intent, requestCodeAccount);
            return;
        } catch (ActivityNotFoundException exception) {
            OPFLog.e("Can't start Samsung authentication activity.", exception);
        }

        postPurchaseResponse(Status.UNKNOWN_ERROR, null);
    }

    @Override
    protected void skuDetails(@NonNull final Set<String> skus) {
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
            postSkuDetailsResponse(Status.SUCCESS, skusDetails);
            return;
        }

        postSkuDetailsResponse(Status.UNKNOWN_ERROR, null);
    }

    @Override
    protected void inventory(final boolean startOver) {
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
            postInventoryResponse(Status.SUCCESS, purchases, false);
            return;
        }

        postInventoryResponse(Status.UNKNOWN_ERROR, null, false);
    }

    @Override
    protected void consume(@NonNull final Purchase purchase) {
        // Samsung doesn't support consume http://developer.samsung.com/forum/thread/a/201/244297
        postConsumeResponse(Status.SUCCESS, purchase);
    }

    @Override
    protected void onActivityResult(@NonNull final Activity activity,
                                    final int requestCode,
                                    final int resultCode,
                                    @Nullable final Intent data) {
        if (requestCode == requestCodeAccount) {
            if (resultCode == Activity.RESULT_OK) {
                final Intent intent = SamsungUtils.getPurchaseIntent(context,
                                                                     skuResolver.getGroupId(),
                                                                     pendingPurchaseSku);
                activity.startActivityForResult(intent, requestCodePurchase);
                return;
            } else {
                postPurchaseResponse(Status.USER_CANCELED, null);
            }
        } else if (requestCode == requestCodePurchase) {
            final SamsungPurchase samsungPurchase;
            final Bundle bundle = data.getExtras();
            final Status error = SamsungUtils.getStatusForError(context, bundle);
            if (error != null) {
                postPurchaseResponse(error, null);
            } else if (requestCode != Activity.RESULT_OK ||
                    (samsungPurchase = SamsungUtils.getPurchase(bundle)) == null) {
                postPurchaseResponse(Status.UNKNOWN_ERROR, null);
            } else {
                final Purchase purchase = SamsungUtils.convertPurchase(INFO, samsungPurchase);
                postPurchaseResponse(Status.SUCCESS, purchase);
            }
        }
        super.onActivityResult(activity, requestCode, resultCode, data);
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
