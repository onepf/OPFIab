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

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.Signature;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;
import org.onepf.opfiab.samsung.model.ItemType;
import org.onepf.opfiab.samsung.model.SamsungItem;
import org.onepf.opfiab.util.OPFIabUtils;

import java.util.ArrayList;
import java.util.Collection;


public final class SamsungUtils {

    private static final int BILLING_SIGNATURE_HASHCODE = 0x7a7eaf4b;
    private static final String BILLING_PACKAGE_NAME = "com.sec.android.iap";
    private static final String BILLING_ACTIVITY = "com.sec.android.iap.activity.PaymentMethodListActivity";

    private static final String KEY_TYPE = "mType";

    private static final String KEY_THIRD_PARTY = "THIRD_PARTY_NAME";
    private static final String KEY_STATUS_CODE = "STATUS_CODE";
    private static final String KEY_ERROR_STRING = "ERROR_STRING";
    private static final String KEY_IAP_UPGRADE_URL = "IAP_UPGRADE_URL";
    private static final String KEY_ITEM_GROUP_ID = "ITEM_GROUP_ID";
    private static final String KEY_ITEM_ID = "ITEM_ID";
    private static final String KEY_RESULT_LIST = "RESULT_LIST";
    private static final String KEY_RESULT_OBJECT = "RESULT_OBJECT";

    private SamsungUtils() {
        throw new IllegalStateException();
    }

    public static boolean checkSignature(@NonNull final Context context) {
        final Signature signature = OPFIabUtils.getPackageSignature(context, BILLING_PACKAGE_NAME);
        return signature != null && signature.hashCode() == BILLING_SIGNATURE_HASHCODE;
    }

    @NonNull
    public static ItemType getItemType(@NonNull final JSONObject jsonObject) throws JSONException {
        final String type = jsonObject.getString(KEY_TYPE);
        final ItemType itemType = ItemType.fromCode(type);
        if (itemType == null) {
            throw new JSONException("Unrecognized item type.");
        }
        return itemType;
    }

    @NonNull
    public static Intent getPurchaseIntent(@NonNull final Context context,
                                           @NonNull final String groupId,
                                           @NonNull final String itemId) {
        final Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);

        final ComponentName component = new ComponentName(BILLING_PACKAGE_NAME, BILLING_ACTIVITY);
        intent.setComponent(component);

        final Bundle bundle = new Bundle();
        final String packageName = context.getPackageName();
        bundle.putString(KEY_THIRD_PARTY, packageName);
        bundle.putString(KEY_ITEM_GROUP_ID, groupId);
        bundle.putString(KEY_ITEM_ID, itemId);
        intent.putExtras(bundle);

        return intent;
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
    public static Collection<SamsungItem> getItems(@Nullable Bundle bundle) {
        if (bundle == null || !bundle.containsKey(KEY_RESULT_LIST)) {
            return null;
        }

        final ArrayList<String> itemsList = bundle.getStringArrayList(KEY_RESULT_LIST);

        return null;
    }
}
