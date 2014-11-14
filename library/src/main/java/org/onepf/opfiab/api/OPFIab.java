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

package org.onepf.opfiab.api;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.onepf.opfiab.*;
import org.onepf.opfiab.model.Options;

public final class OPFIab {

    private OPFIab() {
        throw new UnsupportedOperationException();
    }

    @Nullable
    private static OPFIabHelper instance;

    @NonNull
    public static OPFIabHelper getInstance() {
        checkInit();
        //noinspection ConstantConditions
        return instance;
    }

    @NonNull
    public static ManagedOPFIabHelper getManagedInstance() {
        checkInit();
        return new ManagedOPFIabHelper(instance);
    }

    @NonNull
    public static ActivityOPFIabHelper getActivityInstance() {
        return new ActivityOPFIabHelper(getManagedInstance());
    }

    public static void init(final @NonNull Options options) {
        if (!OPFUtils.uiThread()) {
            throw OPFUtils.wrongThreadException(true);
        }
        if (instance != null) {
            throw new IllegalStateException("init() was already called.");
        }
        instance = new BaseOPFIabHelper(options);
    }

    private static void checkInit() {
        if (instance == null) {
            throw new IllegalStateException("You must call init() first.");
        }
    }
}
