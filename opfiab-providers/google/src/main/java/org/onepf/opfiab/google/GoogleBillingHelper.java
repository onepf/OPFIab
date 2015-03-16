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

package org.onepf.opfiab.google;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.android.vending.billing.IInAppBillingService;

import org.onepf.opfiab.billing.AidlBillingHelper;
import org.onepf.opfiab.google.model.ItemType;
import org.onepf.opfutils.OPFLog;
import org.onepf.opfutils.OPFPreferences;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.onepf.opfiab.google.GoogleBillingProvider.PACKAGE_NAME;

class GoogleBillingHelper extends AidlBillingHelper<IInAppBillingService.Stub> {

    private static final String INTENT_ACTION = "com.android.vending.billing.InAppBillingService.BIND";
    private static final String INTENT_PACKAGE = "com.android.vending";
    private static final String KEY_CONTINUATION_TOKEN = PACKAGE_NAME + ".continuation_token.";


    private static final int API = 3;
    private static final int BATCH_SIZE = 20;


    @NonNull
    private final String packageName;
    @NonNull
    private final OPFPreferences preferences;

    GoogleBillingHelper(@NonNull final Context context) {
        super(context, IInAppBillingService.Stub.class);
        this.packageName = context.getPackageName();
        this.preferences = new OPFPreferences(context, GoogleBillingProvider.NAME);
    }

    @Nullable
    Response isBillingSupported() {
        final IInAppBillingService service = getService();
        if (service == null) {
            return null;
        }
        try {
            for (final ItemType itemType : ItemType.values()) {
                final int code = service.isBillingSupported(API, packageName, itemType.toString());
                final Response response = Response.fromCode(code);
                if (response != Response.OK) {
                    return response;
                }
            }
            return Response.OK;
        } catch (RemoteException exception) {
            OPFLog.e("Billing check failed.", exception);
        }
        return null;
    }

    @Nullable
    Bundle getBuyIntent(@NonNull final String sku, @NonNull final ItemType itemType) {
        final IInAppBillingService service = getService();
        if (service == null) {
            return null;
        }
        try {
            return service.getBuyIntent(API, packageName, sku, itemType.toString(), null);
        } catch (RemoteException exception) {
            OPFLog.e("getBuyIntent request failed.", exception);
        }
        return null;
    }

    @Nullable
    Response consumePurchase(@NonNull final String token) {
        final IInAppBillingService service = getService();
        if (service == null) {
            return null;
        }

        try {
            final int code = service.consumePurchase(API, packageName, token);
            return Response.fromCode(code);
        } catch (RemoteException exception) {
            OPFLog.e("consumePurchase request failed.", exception);
        }
        return null;
    }

    @Nullable
    Bundle getSkuDetails(@NonNull final Collection<String> skus) {
        final IInAppBillingService service = getService();
        if (service == null) {
            return null;
        }
        final List<String> skuList = new ArrayList<>(skus);
        final Bundle result = new Bundle();
        try {
            final int size = skuList.size();
            final int batchCount = size / BATCH_SIZE;
            for (int i = 0; i <= batchCount; i++) {
                final int first = i * BATCH_SIZE;
                final int last = Math.min((i + 1) * BATCH_SIZE, size);
                final ArrayList<String> batch = new ArrayList<>(skuList.subList(first, last));
                final Bundle bundle = GoogleUtils.putSkuList(new Bundle(), batch);
                for (final ItemType itemType : ItemType.values()) {
                    final String type = itemType.toString();
                    final Bundle response = service.getSkuDetails(API, packageName, type, bundle);
                    if (GoogleUtils.getResponse(response) != Response.OK) {
                        return response;
                    } else {
                        final ArrayList<String> skuDetails = GoogleUtils.getSkuDetails(response);
                        GoogleUtils.addSkuDetails(result, skuDetails);
                    }
                }
            }
        } catch (RemoteException exception) {
            OPFLog.e("getSkuDetails request failed.", exception);
            return null;
        }
        return GoogleUtils.putResponse(result, Response.OK);
    }

    @Nullable
    Bundle getPurchases(final boolean startOver) {
        final IInAppBillingService service = getService();
        if (service == null) {
            return null;
        }
        final Bundle result = new Bundle();
        try {
            for (final ItemType itemType : ItemType.values()) {
                final String type = itemType.toString();
                final String key = KEY_CONTINUATION_TOKEN + type;
                final String token = startOver ? null : preferences.getString(key);
                final Bundle response = service.getPurchases(API, packageName, type, token);
                if (GoogleUtils.getResponse(response) != Response.OK) {
                    return response;
                } else {
                    final ArrayList<String> purchaseDataList = GoogleUtils.getDataList(
                            response);
                    final ArrayList<String> itemList = GoogleUtils.getItemList(response);
                    final ArrayList<String> signatureList = GoogleUtils.getSignatureList(response);
                    final String newToken = GoogleUtils.getContinuationToken(response);
                    GoogleUtils.addDataList(result, purchaseDataList);
                    GoogleUtils.addItemList(result, itemList);
                    GoogleUtils.addSignatureList(result, signatureList);
                    if (TextUtils.isEmpty(newToken)) {
                        preferences.remove(key);
                    } else {
                        preferences.put(key, newToken);
                    }
                }
            }
            return result;
        } catch (RemoteException exception) {
            OPFLog.e("getPurchases request failed.", exception);
        }
        return GoogleUtils.putResponse(result, Response.OK);
    }

    @NonNull
    @Override
    protected Intent getServiceIntent() {
        final Intent serviceIntent = new Intent(INTENT_ACTION);
        serviceIntent.setPackage(INTENT_PACKAGE);
        return serviceIntent;
    }
}
