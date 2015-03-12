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

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;

final class Utils {

    private static final String RESPONSE_CODE = "RESPONSE_CODE";
    private static final String SKU_DETAILS_LIST = "DETAILS_LIST";
    private static final String SKU_LIST = "ITEM_ID_LIST";
    private static final String BUY_INTENT = "BUY_INTENT";
    private static final String PURCHASE_DATA = "INAPP_PURCHASE_DATA";
    private static final String SIGNATURE = "INAPP_DATA_SIGNATURE";
    private static final String ITEM_LIST = "INAPP_PURCHASE_ITEM_LIST";
    private static final String PURCHASE_DATA_LIST = "INAPP_PURCHASE_DATA_LIST";
    private static final String SIGNATURE_LIST = "INAPP_DATA_SIGNATURE_LIST";
    private static final String CONTINUATION_TOKEN = "INAPP_CONTINUATION_TOKEN";


    @Nullable
    private static ArrayList<String> getList(@Nullable final Bundle bundle,
                                             @NonNull final String key) {
        if (bundle != null && bundle.containsKey(key)) {
            return bundle.getStringArrayList(key);
        }
        return null;
    }

    @NonNull
    private static Bundle putList(@NonNull final Bundle bundle,
                                  @Nullable final ArrayList<String> list,
                                  @NonNull final String key) {
        if (list != null && !list.isEmpty()) {
            bundle.putStringArrayList(key, list);
        }
        return bundle;
    }

    @NonNull
    private static Bundle addList(@NonNull final Bundle bundle,
                                  @Nullable final ArrayList<String> list,
                                  @NonNull final String key) {
        if (list != null && !list.isEmpty()) {
            final ArrayList<String> newList;
            final ArrayList<String> oldList;
            if ((oldList = getList(bundle, key)) == null) {
                newList = list;
            } else {
                newList = new ArrayList<>(oldList);
                newList.addAll(list);
            }
            bundle.putStringArrayList(key, newList);
        }
        return bundle;
    }

    @NonNull
    static Bundle addSkuDetails(@NonNull final Bundle bundle,
                                @Nullable final ArrayList<String> skuDetailsList) {
        return addList(bundle, skuDetailsList, SKU_DETAILS_LIST);
    }

    @Nullable
    static ArrayList<String> getSkuDetails(@Nullable final Bundle bundle) {
        return getList(bundle, SKU_DETAILS_LIST);
    }

    @NonNull
    static Bundle addPurchaseDataList(@NonNull final Bundle bundle,
                                      @Nullable final ArrayList<String> purchaseData) {
        return addList(bundle, purchaseData, PURCHASE_DATA_LIST);
    }

    @Nullable
    static ArrayList<String> getPurchaseDataList(@Nullable final Bundle bundle) {
        return getList(bundle, PURCHASE_DATA_LIST);
    }

    @NonNull
    static Bundle addItemList(@NonNull final Bundle bundle,
                              @Nullable final ArrayList<String> purchaseData) {
        return addList(bundle, purchaseData, ITEM_LIST);
    }

    @Nullable
    static ArrayList<String> getItemList(@Nullable final Bundle bundle) {
        return getList(bundle, ITEM_LIST);
    }

    @NonNull
    static Bundle addSignatureList(@NonNull final Bundle bundle,
                                   @Nullable final ArrayList<String> purchaseData) {
        return addList(bundle, purchaseData, SIGNATURE_LIST);
    }

    @Nullable
    static ArrayList<String> getSignatureList(@Nullable final Bundle bundle) {
        return getList(bundle, SIGNATURE_LIST);
    }

    @NonNull
    static Bundle putSkuList(@NonNull final Bundle bundle,
                             @Nullable final ArrayList<String> skuList) {
        return putList(bundle, skuList, SKU_LIST);
    }

    @Nullable
    static Response getResponse(@Nullable final Bundle bundle) {
        if (bundle != null && bundle.containsKey(RESPONSE_CODE)) {
            final int responseCode = bundle.getInt(RESPONSE_CODE);
            return Response.fromCode(responseCode);
        }
        return null;
    }

    @NonNull
    static Bundle putResponse(@NonNull final Bundle bundle,
                              @NonNull final Response response) {
        bundle.putInt(RESPONSE_CODE, response.code);
        return bundle;
    }

    @Nullable
    static String getContinuationToken(@Nullable final Bundle bundle) {
        if (bundle != null && bundle.containsKey(CONTINUATION_TOKEN)) {
            return bundle.getString(CONTINUATION_TOKEN);
        }
        return null;
    }


    private Utils() {
        throw new UnsupportedOperationException();
    }
}
