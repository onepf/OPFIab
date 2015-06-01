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

import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.Signature;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;
import org.onepf.opfiab.model.BillingProviderInfo;
import org.onepf.opfiab.model.billing.Purchase;
import org.onepf.opfiab.model.billing.SkuDetails;
import org.onepf.opfiab.model.billing.SkuType;
import org.onepf.opfiab.model.event.billing.Status;
import org.onepf.opfiab.samsung.model.ItemType;
import org.onepf.opfiab.samsung.model.SamsungPurchase;
import org.onepf.opfiab.samsung.model.SamsungPurchasedItem;
import org.onepf.opfiab.samsung.model.SamsungSkuDetails;
import org.onepf.opfiab.util.OPFIabUtils;
import org.onepf.opfutils.OPFLog;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public final class SamsungUtils {

    private static final int BILLING_SIGNATURE_HASHCODE = 0x7a7eaf4b;
    private static final String BILLING_PACKAGE_NAME = "com.sec.android.iap";
    private static final String ACCOUNT_ACTIVITY = "com.sec.android.iap.activity.AccountActivity";
    private static final String PURCHASE_ACTIVITY = "com.sec.android.iap.activity.PaymentMethodListActivity";

    private static final String KEY_TYPE = "mType";

    private static final String KEY_THIRD_PARTY = "THIRD_PARTY_NAME";
    private static final String KEY_STATUS_CODE = "STATUS_CODE";
    private static final String KEY_ERROR_STRING = "ERROR_STRING";
    private static final String KEY_IAP_UPGRADE_URL = "IAP_UPGRADE_URL";
    private static final String KEY_ITEM_GROUP_ID = "ITEM_GROUP_ID";
    private static final String KEY_ITEM_ID = "ITEM_ID";
    private static final String KEY_RESULT_LIST = "RESULT_LIST";
    private static final String KEY_RESULT_OBJECT = "RESULT_OBJECT";

    private static final ThreadLocal<DateFormat> DATE_FORMAT = new ThreadLocal<DateFormat>() {
        @Override
        protected DateFormat initialValue() {
            return new SimpleDateFormat("yyyyMMdd", Locale.getDefault());
        }
    };

    private SamsungUtils() {
        throw new IllegalStateException();
    }

    public static boolean checkSignature(@NonNull final Context context) {
        final Signature signature = OPFIabUtils.getPackageSignature(context, BILLING_PACKAGE_NAME);
        final boolean result = signature != null && signature.hashCode() == BILLING_SIGNATURE_HASHCODE;
        if (!result) {
            OPFLog.e("Samsung signature check failed.");
        }
        return result;
    }

    @Nullable
    public static Status getStatusForError(@NonNull final Context context,
                                           @Nullable final Bundle bundle) {
        final Response response = getResponse(bundle);
        if (response == Response.ERROR_NONE) {
            return null;
        }
        if (bundle == null || response == null) {
            return Status.UNKNOWN_ERROR;
        }
        OPFLog.e("Response %s: %s", response, getErrorString(bundle));
        switch (response) {
            case PAYMENT_IS_CANCELED:
                return Status.USER_CANCELED;
            case ERROR_ALREADY_PURCHASED:
                return Status.ITEM_ALREADY_OWNED;
            case ERROR_PRODUCT_DOES_NOT_EXIST:
            case ERROR_ITEM_GROUP_ID_DOES_NOT_EXIST:
                return Status.ITEM_UNAVAILABLE;
            case ERROR_NEED_APP_UPGRADE:
                SamsungUtils.promptUpgrade(context, bundle);
            case ERROR_NETWORK_NOT_AVAILABLE:
            case ERROR_CONNECT_TIMEOUT:
            case ERROR_SOCKET_TIMEOUT:
                return Status.SERVICE_UNAVAILABLE;
            default:
                return Status.UNKNOWN_ERROR;
        }
    }

    public static void promptUpgrade(@NonNull final Context context,
                                     @Nullable final Bundle bundle) {
        final String uri;
        if (bundle == null || (uri = bundle.getString(KEY_IAP_UPGRADE_URL)) == null) {
            OPFLog.e("No upgrade url.");
            return;
        }

        final Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(uri));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            context.startActivity(intent);
        } catch (ActivityNotFoundException exception) {
            OPFLog.e("Failed to open upgrade activity", exception);
        }
    }

    @NonNull
    public static String getNowDate() {
        //noinspection AccessToNonThreadSafeStaticField
        return DATE_FORMAT.get().format(new Date());
    }

    @Nullable
    public static Date parseDate(@Nullable final String date) {
        if (date == null) {
            return null;
        }
        try {
            return DATE_FORMAT.get().parse(date);
        } catch (ParseException e) {
            return null;
        }
    }

    @NonNull
    public static Intent getAccountIntent() {
        final Intent intent = new Intent();
        final ComponentName component = new ComponentName(BILLING_PACKAGE_NAME, ACCOUNT_ACTIVITY);
        intent.setComponent(component);
        return intent;
    }

    @NonNull
    public static Intent getPurchaseIntent(@NonNull final Context context,
                                           @NonNull final String groupId,
                                           @NonNull final String itemId) {
        final Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);

        final ComponentName component = new ComponentName(BILLING_PACKAGE_NAME, PURCHASE_ACTIVITY);
        intent.setComponent(component);

        final Bundle bundle = new Bundle();
        final String packageName = context.getPackageName();
        bundle.putString(KEY_THIRD_PARTY, packageName);
        bundle.putString(KEY_ITEM_GROUP_ID, groupId);
        bundle.putString(KEY_ITEM_ID, itemId);
        intent.putExtras(bundle);

        return intent;
    }

    @NonNull
    public static ItemType getItemType(@NonNull final JSONObject jsonObject) throws JSONException {
        final String type = jsonObject.getString(KEY_TYPE);
        final ItemType itemType = ItemType.fromCode(type);
        if (itemType == null) {
            throw new JSONException("Unrecognized item type: " + type);
        }
        return itemType;
    }

    @Nullable
    public static Response getResponse(@Nullable final Bundle bundle) {
        if (bundle != null && bundle.containsKey(KEY_STATUS_CODE)) {
            final int code = bundle.getInt(KEY_STATUS_CODE);
            return Response.fromCode(code);
        }
        return null;
    }

    @Nullable
    public static String getErrorString(@Nullable final Bundle bundle) {
        if (bundle != null && bundle.containsKey(KEY_ERROR_STRING)) {
            return bundle.getString(KEY_ERROR_STRING);
        }
        return null;
    }

    @Nullable
    private static Collection<String> getItems(@Nullable final Bundle bundle) {
        final List<String> items;
        if (bundle == null || (items = bundle.getStringArrayList(KEY_RESULT_LIST)) == null) {
            return null;
        }

        if (items.isEmpty()) {
            Collections.emptyList();
        }

        return items;
    }

    @Nullable
    public static Collection<SamsungPurchasedItem> getPurchasedItems(
            @Nullable final Bundle bundle) {
        final Collection<String> items = getItems(bundle);
        if (items == null) {
            return null;
        }

        final Collection<SamsungPurchasedItem> samsungItems = new ArrayList<>(items.size());
        for (final String item : items) {
            try {
                samsungItems.add(new SamsungPurchasedItem(item));
            } catch (JSONException exception) {
                OPFLog.e("Filed to decode Samsung inventory item", exception);
            }
        }

        return samsungItems;
    }

    @Nullable
    public static Collection<SamsungSkuDetails> getSkusDetails(@Nullable final Bundle bundle) {
        final Collection<String> items = getItems(bundle);
        if (items == null) {
            return null;
        }

        final Collection<SamsungSkuDetails> samsungItems = new ArrayList<>(items.size());
        for (final String item : items) {
            try {
                samsungItems.add(new SamsungSkuDetails(item));
            } catch (JSONException exception) {
                OPFLog.e("Filed to decode Samsung inventory item", exception);
            }
        }

        return samsungItems;
    }

    @Nullable
    public static Bundle putItems(@NonNull final Bundle from,
                                  @Nullable final Bundle to) {
        final Collection<String> fromItems = getItems(from);
        if (fromItems == null || fromItems.isEmpty() || to == null) {
            return to;
        }
        final ArrayList<String> mergedItems = new ArrayList<>(fromItems);
        final Collection<String> toItems = getItems(to);
        if (toItems != null) {
            mergedItems.addAll(toItems);
        }
        to.putStringArrayList(KEY_RESULT_LIST, mergedItems);
        return to;
    }

    @Nullable
    public static SamsungPurchase getPurchase(@Nullable final Bundle bundle) {
        final String item;
        if (bundle == null || (item = bundle.getString(KEY_RESULT_OBJECT)) == null) {
            return null;
        }
        try {
            return new SamsungPurchase(item);
        } catch (JSONException exception) {
            OPFLog.e("Failed to decode Samsung purchase.", exception);
        }
        return null;
    }

    @NonNull
    public static SkuType convertType(@NonNull final ItemType itemType) {
        switch (itemType) {
            case CONSUMABLE:
                return SkuType.CONSUMABLE;
            case NON_CONSUMABLE:
                return SkuType.ENTITLEMENT;
            case SUBSCRIPTION:
                return SkuType.SUBSCRIPTION;
            default:
                return SkuType.UNKNOWN;
        }
    }

    @NonNull
    public static SkuDetails convertSkuDetails(@NonNull final BillingProviderInfo info,
                                               @NonNull final SamsungSkuDetails samsungSkuDetails) {
        return new SkuDetails.Builder(samsungSkuDetails.getItemId())
                .setOriginalJson(samsungSkuDetails.getOriginalJson())
                .setType(convertType(samsungSkuDetails.getItemType()))
                .setTitle(samsungSkuDetails.getName())
                .setDescription(samsungSkuDetails.getDescription())
                .setPrice(samsungSkuDetails.getPriceString())
                .setProviderInfo(info)
                .build();
    }

    @NonNull
    public static Purchase convertPurchasedItems(@NonNull final BillingProviderInfo info,
                                                 @NonNull final SamsungPurchasedItem purchasedItem) {
        final Date endDate = purchasedItem.getSubscriptionEndDate();
        return new Purchase.Builder(purchasedItem.getItemId())
                .setOriginalJson(purchasedItem.getOriginalJson())
                .setType(convertType(purchasedItem.getItemType()))
                .setToken(purchasedItem.getPaymentId())
                .setPurchaseTime(purchasedItem.getPurchaseDate().getTime())
                .setCanceled(endDate != null && endDate.before(new Date()))
                .setProviderInfo(info)
                .build();
    }

    @NonNull
    public static Purchase convertPurchase(@NonNull final BillingProviderInfo info,
                                           @NonNull final SamsungPurchase purchase) {
        return new Purchase.Builder(purchase.getItemId())
                .setOriginalJson(purchase.getOriginalJson())
                // TODO
                .setType(SkuType.UNKNOWN)
                .setToken(purchase.getPaymentId())
                .setPurchaseTime(purchase.getPurchaseDate().getTime())
                .setProviderInfo(info)
                .build();
    }
}
