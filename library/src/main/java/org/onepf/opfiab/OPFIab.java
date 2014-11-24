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

package org.onepf.opfiab;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.onepf.opfiab.billing.BillingProvider;
import org.onepf.opfiab.model.Options;
import org.onepf.opfiab.util.OPFUtils;

public final class OPFIab {

    private OPFIab() {
        throw new UnsupportedOperationException();
    }

    @NonNull
    static BaseOPFIabHelper instance;

    @NonNull
    public static OPFIabHelper getInstance() {
        checkInit();
        OPFUtils.checkThread(true);
        //noinspection ConstantConditions
        return instance;
    }

    @NonNull
    public static ManagedOPFIabHelper getManagedInstance() {
        OPFUtils.checkThread(true);
        checkInit();
        return new ManagedOPFIabHelper(instance);
    }

    @NonNull
    public static ActivityOPFIabHelper getActivityInstance(@NonNull final Activity activity) {
        return new ActivityOPFIabHelper(getManagedInstance(), activity);
    }

    @Nullable
    public static BillingProvider getCurrentProvider() {
        checkInit();
        return null;
    }

    public static void init(@NonNull final Context context, @NonNull final Options options) {
        OPFUtils.checkThread(true);
        //noinspection ConstantConditions
        if (instance != null) {
            throw new IllegalStateException("init() was already called.");
        }
        instance = new BaseOPFIabHelper(context, options);
        OPFIabBroadcast.init(context);
    }

    public static void setup() {

    }



    private static void checkInit() {
        //noinspection ConstantConditions
        if (instance == null) {
            throw OPFUtils.notInitException();
        }
    }
}
