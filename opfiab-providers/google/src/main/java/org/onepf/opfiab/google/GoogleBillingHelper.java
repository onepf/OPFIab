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

import com.android.vending.billing.IInAppBillingService;

import org.onepf.opfiab.billing.AidlBillingHelper;
import org.onepf.opfiab.google.model.ItemType;
import org.onepf.opfutils.OPFLog;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

class GoogleBillingHelper extends AidlBillingHelper<IInAppBillingService.Stub> {

    private static final String ACTION = "com.android.vending.billing.InAppBillingService.BIND";
    private static final String PACKAGE_NAME = "com.android.vending";

    private static final int API = 3;
    private static final int BATCH_SIZE = 20;


    @NonNull
    private final String packageName;

    GoogleBillingHelper(@NonNull final Context context) {
        super(context, IInAppBillingService.Stub.class);
        this.packageName = context.getPackageName();
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
            OPFLog.e("", exception);
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
            for (int i = 0; i < batchCount; i++) {
                final int first = i * BATCH_SIZE;
                final int last = Math.min((i + 1) * BATCH_SIZE, size);
                final ArrayList<String> batch = new ArrayList<>(skuList.subList(first, last));
                final Bundle bundle = GoogleUtils.putSkuList(new Bundle(), batch);
                for (final ItemType itemType : ItemType.values()) {
                    final String type = itemType.toString();
                    final Bundle response = service.getSkuDetails(API, packageName, type, bundle);
                    if (GoogleUtils.getResponse(response) != Response.OK) {
                        return response;
                    }
                    final ArrayList<String> skuDetails = GoogleUtils.getSkuDetails(response);
                    if (skuDetails != null) {
                        GoogleUtils.addSkuDetails(result, skuDetails);
                    }
                }
            }
        } catch (RemoteException exception) {
            OPFLog.e("", exception);
            return null;
        }
        return GoogleUtils.putResponse(result, Response.OK);
    }

    @NonNull
    @Override
    protected Intent getServiceIntent() {
        final Intent serviceIntent = new Intent(ACTION);
        serviceIntent.setPackage(PACKAGE_NAME);
        return serviceIntent;
    }
}
