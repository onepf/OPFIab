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

final class GoogleUtils {

    private static final String RESPONSE_CODE = "RESPONSE_CODE";
    private static final String SKU_DETAILS_LIST = "DETAILS_LIST";
    private static final String SKU_LIST = "ITEM_ID_LIST";
    private static final String BUY_INTENT = "BUY_INTENT";
    private static final String INAPP_PURCHASE_DATA = "INAPP_PURCHASE_DATA";
    private static final String INAPP_SIGNATURE = "INAPP_DATA_SIGNATURE";
    private static final String INAPP_ITEM_LIST = "INAPP_PURCHASE_ITEM_LIST";
    private static final String INAPP_PURCHASE_DATA_LIST = "INAPP_PURCHASE_DATA_LIST";
    private static final String INAPP_SIGNATURE_LIST = "INAPP_DATA_SIGNATURE_LIST";
    private static final String CONTINUATION_TOKEN = "INAPP_CONTINUATION_TOKEN";


    @NonNull
    static Bundle addSkuDetails(@NonNull final Bundle bundle,
                                @NonNull final ArrayList<String> skuDetailsList) {
        final ArrayList<String> newList;
        final ArrayList<String> oldList;
        if ((oldList = getSkuDetails(bundle)) == null) {
            newList = skuDetailsList;
        } else {
            newList = new ArrayList<>(oldList);
            newList.addAll(skuDetailsList);
        }
        bundle.putStringArrayList(SKU_DETAILS_LIST, newList);
        return bundle;
    }

    @Nullable
    static ArrayList<String> getSkuDetails(@Nullable final Bundle bundle) {
        if (bundle != null && bundle.containsKey(SKU_DETAILS_LIST)) {
            return bundle.getStringArrayList(SKU_DETAILS_LIST);
        }
        return null;
    }

    @NonNull
    static Bundle putSkuList(@NonNull final Bundle bundle,
                             @NonNull final ArrayList<String> skuList) {
        bundle.putStringArrayList(SKU_LIST, skuList);
        return bundle;
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


    private GoogleUtils() {
        throw new UnsupportedOperationException();
    }
}
