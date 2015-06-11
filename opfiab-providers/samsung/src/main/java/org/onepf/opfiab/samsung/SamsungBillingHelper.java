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
import android.os.Bundle;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.sec.android.iap.IAPConnector;

import org.onepf.opfiab.billing.AidlBillingHelper;
import org.onepf.opfiab.samsung.model.ItemType;
import org.onepf.opfutils.OPFLog;
import org.onepf.opfutils.OPFUtils;

final class SamsungBillingHelper extends AidlBillingHelper<IAPConnector> {

    private static final String IAP_PACKAGE = "com.sec.android.iap";
    private static final String SERVICE_CLASS = "com.sec.android.iap.service.IAPService";

    private static final String START_DATE = "20130101";


    private final int billingMode;
    @NonNull
    private final String packageName;

    public SamsungBillingHelper(@NonNull final Context context,
            @NonNull final BillingMode billingMode) {
        super(context, IAPConnector.class);
        this.billingMode = billingMode.getCode();
        this.packageName = context.getPackageName();
    }

    @Nullable
    @Override
    public IAPConnector getService() {
        if (!OPFUtils.isInstalled(context, IAP_PACKAGE)) {
            SamsungUtils.promptInstall(context);
            return null;
        }
        return super.getService();
    }

    @NonNull
    @Override
    protected Intent getServiceIntent() {
        final Intent intent = new Intent();
        intent.setComponent(new ComponentName(IAP_PACKAGE, SERVICE_CLASS));
        return intent;
    }

    @Nullable
    public Bundle init() {
        OPFLog.logMethod();
        final IAPConnector iapConnector = getService();
        if (iapConnector == null) {
            return null;
        }
        try {
            final Bundle bundle = iapConnector.init(billingMode);
            return SamsungUtils.checkSignature(context) ? bundle : null;
        } catch (RemoteException exception) {
            OPFLog.e("init failed.", exception);
        }
        return null;
    }

    @Nullable
    public Bundle getItemsInbox(@NonNull final String groupId) {
        OPFLog.logMethod();
        final IAPConnector iapConnector = getService();
        if (iapConnector == null) {
            return null;
        }
        try {
            return iapConnector.getItemsInbox(packageName, groupId, 1, Integer.MAX_VALUE,
                    START_DATE, SamsungUtils.getNowDate());
        } catch (RemoteException exception) {
            OPFLog.e("getItemsInbox failed.", exception);
        }
        return null;
    }

    @Nullable
    public Bundle getItemList(@NonNull final String groupId) {
        OPFLog.logMethod();
        final IAPConnector iapConnector = getService();
        if (iapConnector == null) {
            return null;
        }
        try {
            return iapConnector.getItemList(billingMode, packageName, groupId, 1, Integer.MAX_VALUE,
                    ItemType.ALL.getCode());
        } catch (RemoteException exception) {
            OPFLog.e("getItemList failed.", exception);
        }
        return null;
    }
}
