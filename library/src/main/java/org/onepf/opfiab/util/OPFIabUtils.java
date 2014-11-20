/*
 * Copyright 2012-2014 One Platform Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.onepf.opfiab.util;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.onepf.opfiab.OPFIabAction;
import org.onepf.opfiab.billing.ResponseStatus;
import org.onepf.opfiab.billing.SetupStatus;
import org.onepf.opfiab.model.Inventory;
import org.onepf.opfiab.model.Purchase;
import org.onepf.opfiab.model.SkuInfo;
import org.onepf.opfiab.model.response.Response;

import java.util.ArrayList;
import java.util.Collection;

@SuppressWarnings("unchecked")
public final class OPFIabUtils {

    private OPFIabUtils() {
        throw new UnsupportedOperationException();
    }


    private static final String packageName = OPFIabUtils.class.getPackage().getName();

    private static final String EXTRA_ACTION = packageName + ".action";
    private static final String EXTRA_RESPONSE = packageName + ".response";
    private static final String EXTRA_RESPONSE_STATUS = packageName + ".responseStatus";
    private static final String EXTRA_SETUP_STATUS = packageName + ".setupStatus";
    private static final String EXTRA_INVENTORY = packageName + ".inventory";
    private static final String EXTRA_PURCHASE = packageName + ".purchase";
    private static final String EXTRA_SKU_INFO = packageName + ".skuInfo";
    private static final String EXTRA_SKU_INFOS = packageName + ".skuInfos";

    public static void putAction(@NonNull final Bundle bundle, @NonNull final OPFIabAction action) {
        bundle.putSerializable(EXTRA_ACTION, action);
    }

    public static void putResponse(@NonNull final Bundle bundle, @NonNull final Response response) {
        bundle.putSerializable(EXTRA_RESPONSE, response);
    }

    public static void putResponseStatus(@NonNull final Bundle bundle,
                                         @NonNull final ResponseStatus responseStatus) {
        bundle.putSerializable(EXTRA_RESPONSE_STATUS, responseStatus);
    }

    public static void putSetupStatus(@NonNull final Bundle bundle,
                                      @NonNull final SetupStatus setupStatus) {
        bundle.putSerializable(EXTRA_SETUP_STATUS, setupStatus);
    }

    public static void putInventory(@NonNull final Bundle bundle,
                                    @NonNull final Inventory inventory) {
        bundle.putSerializable(EXTRA_INVENTORY, inventory);
    }

    public static void putPurchase(@NonNull final Bundle bundle, @NonNull final Purchase purchase) {
        bundle.putSerializable(EXTRA_PURCHASE, purchase);
    }

    public static void putSkuInfo(@NonNull final Bundle bundle, @NonNull final SkuInfo skuInfo) {
        bundle.putSerializable(EXTRA_SKU_INFO, skuInfo);
    }

    public static void putSkuInfos(@NonNull final Bundle bundle,
                                   @NonNull final Collection<SkuInfo> skuInfos) {
        bundle.putSerializable(EXTRA_SKU_INFOS, new ArrayList<>(skuInfos));
    }


    @Nullable
    public static OPFIabAction getAction(@NonNull final Bundle bundle) {
        return (OPFIabAction) bundle.getSerializable(EXTRA_ACTION);
    }

    @Nullable
    public static Response getResponse(@NonNull final Bundle bundle) {
        return (Response) bundle.getSerializable(EXTRA_RESPONSE);
    }

    @Nullable
    public static ResponseStatus getResponseStatus(@NonNull final Bundle bundle) {
        return (ResponseStatus) bundle.getSerializable(EXTRA_RESPONSE_STATUS);
    }

    @Nullable
    public static SetupStatus getSetupStatus(@NonNull final Bundle bundle) {
        return (SetupStatus) bundle.getSerializable(EXTRA_SETUP_STATUS);
    }

    @Nullable
    public static Inventory getInventory(@NonNull final Bundle bundle) {
        return (Inventory) bundle.getSerializable(EXTRA_INVENTORY);
    }

    @Nullable
    public static Purchase getPurchase(@NonNull final Bundle bundle) {
        return (Purchase) bundle.getSerializable(EXTRA_PURCHASE);
    }

    @Nullable
    public static SkuInfo getSkuInfo(@NonNull final Bundle bundle) {
        return (SkuInfo) bundle.getSerializable(EXTRA_SKU_INFO);
    }

    @Nullable
    public static ArrayList<SkuInfo> getSkuInfos(@NonNull final Bundle bundle) {
        return (ArrayList<SkuInfo>) bundle.getSerializable(EXTRA_SKU_INFO);
    }


    @Nullable
    public static OPFIabAction getAction(@NonNull final Intent intent) {
        final Bundle extras = intent.getExtras();
        return extras == null ? null : getAction(extras);
    }

    @Nullable
    public static Response getResponse(@NonNull final Intent intent) {
        final Bundle extras = intent.getExtras();
        return extras == null ? null : getResponse(extras);
    }

    @Nullable
    public static ResponseStatus getResponseStatus(@NonNull final Intent intent) {
        final Bundle extras = intent.getExtras();
        return extras == null ? null : getResponseStatus(extras);
    }

    @Nullable
    public static SetupStatus getSetupStatus(@NonNull final Intent intent) {
        final Bundle extras = intent.getExtras();
        return extras == null ? null : getSetupStatus(extras);
    }

    @Nullable
    public static Inventory getInventory(@NonNull final Intent intent) {
        final Bundle extras = intent.getExtras();
        return extras == null ? null : getInventory(extras);
    }

    @Nullable
    public static Purchase getPurchase(@NonNull final Intent intent) {
        final Bundle extras = intent.getExtras();
        return extras == null ? null : getPurchase(extras);
    }

    @Nullable
    public static SkuInfo getSkuInfo(@NonNull final Intent intent) {
        final Bundle extras = intent.getExtras();
        return extras == null ? null : getSkuInfo(extras);
    }

    @Nullable
    public static ArrayList<SkuInfo> getSkuInfos(@NonNull final Intent intent) {
        final Bundle extras = intent.getExtras();
        return extras == null ? null : getSkuInfos(extras);
    }
}
