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
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.android.vending.billing.IInAppBillingService;

import org.onepf.opfiab.billing.AidlBillingHelper;
import org.onepf.opfutils.OPFLog;

class GoogleAidlBillingHelper extends AidlBillingHelper<IInAppBillingService.Stub> {

    private static final String ACTION = "com.android.vending.billing.InAppBillingService.BIND";
    private static final String PACKAGE_NAME = "com.android.vending";

    private static final String ITEM_TYPE_INAPP = "inapp";
    private static final String ITEM_TYPE_SUBS = "subs";


    @NonNull
    private final String packageName;

    GoogleAidlBillingHelper(@NonNull final Context context) {
        super(context, IInAppBillingService.Stub.class);
        this.packageName = context.getPackageName();
    }

    @Nullable
    Response isBillingSupported() {
        final IInAppBillingService service = getService();
        if (service != null) {
            try {
                final int code = service.isBillingSupported(3, packageName, ITEM_TYPE_INAPP);
                return Response.fromCode(code);
            } catch (RemoteException exception) {
                OPFLog.e("", exception);
            }
        }
        return null;
    }

    @NonNull
    @Override
    protected Intent getServiceIntent() {
        final Intent serviceIntent = new Intent(ACTION);
        serviceIntent.setPackage(PACKAGE_NAME);
        return serviceIntent;
    }
}
