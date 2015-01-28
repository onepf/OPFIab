/*
 * Copyright 2012-2014 One Platform Foundation
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

package org.onepf.opfiab.model.event.request;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.onepf.opfiab.model.billing.SkuDetails;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;

public class PurchaseRequest extends Request {

    @Nullable
    private final transient Reference<Activity> activityReference;

    @NonNull
    private final SkuDetails skuDetails;

    public PurchaseRequest(@NonNull final Activity activity, @NonNull final SkuDetails skuDetails) {
        super(Type.PURCHASE);
        this.activityReference = new WeakReference<Activity>(activity);
        this.skuDetails = skuDetails;
    }

    @Nullable
    public Activity getActivity() {
        return activityReference == null ? null : activityReference.get();
    }

    @NonNull
    public SkuDetails getSkuDetails() {
        return skuDetails;
    }
}
