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

package org.onepf.opfiab;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.onepf.opfiab.android.OPFIabActivity;
import org.onepf.opfiab.api.IabHelper;
import org.onepf.opfiab.api.SimpleIabHelper;
import org.onepf.opfiab.model.event.android.ActivityResultEvent;
import org.onepf.opfiab.model.event.billing.PurchaseRequest;

/**
 * This implementation of {@link IabHelper} allows passing existing {@link Activity} object thus
 * avoiding usage of {@link OPFIabActivity}.
 * <p>
 * Supplied Activity <b>must</b> delegate {@link Activity#onActivityResult(int, int, Intent)}
 * callback to this helper.
 *
 * @see #purchase(Activity, String)
 */
class SimpleIabHelperImpl extends IabHelperImpl implements SimpleIabHelper {

    @Override
    public void purchase(@NonNull final Activity activity, @NonNull final String sku) {
        postRequest(new PurchaseRequest(activity, true, sku));
    }

    @Override
    public void onActivityResult(@NonNull final Activity activity, final int requestCode,
                                 final int resultCode, @Nullable final Intent data) {
        OPFIab.post(new ActivityResultEvent(activity, requestCode, resultCode, data));
    }
}
