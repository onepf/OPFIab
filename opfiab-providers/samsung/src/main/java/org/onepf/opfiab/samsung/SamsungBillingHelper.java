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
import android.support.annotation.NonNull;

import com.sec.android.iap.IAPConnector;

import org.onepf.opfiab.billing.AidlBillingHelper;

class SamsungBillingHelper extends AidlBillingHelper<IAPConnector> {

    private static final String INTENT_PACKAGE = "com.sec.android.iap";
    private static final String INTENT_CLASS = "com.sec.android.iap.service.IAPService";


    @NonNull
    private final String packageName;
    @NonNull
    private final BillingMode billingMode;

    public SamsungBillingHelper(@NonNull final Context context,
                                @NonNull final BillingMode billingMode) {
        super(context, IAPConnector.class);
        this.billingMode = billingMode;
        this.packageName = context.getPackageName();
    }

    @NonNull
    @Override
    protected Intent getServiceIntent() {
        final Intent intent = new Intent();
        intent.setComponent(new ComponentName(INTENT_PACKAGE, INTENT_CLASS));
        return intent;
    }
}
