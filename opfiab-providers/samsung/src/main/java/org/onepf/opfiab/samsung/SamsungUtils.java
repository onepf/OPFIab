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

import android.content.Context;
import android.content.pm.Signature;
import android.support.annotation.NonNull;

import org.onepf.opfiab.util.OPFIabUtils;

public final class SamsungUtils {

    private static final int BILLING_SIGNATURE_HASHCODE = 0x7a7eaf4b;
    private static final String BILLING_PACKAGE_NAME = "com.sec.android.iap";

    public static boolean checkSignature(@NonNull final Context context) {
        final Signature signature = OPFIabUtils.getPackageSignature(context, BILLING_PACKAGE_NAME);
        return signature != null && signature.hashCode() == BILLING_SIGNATURE_HASHCODE;
    }

    private SamsungUtils() {
        throw new IllegalStateException();
    }
}
