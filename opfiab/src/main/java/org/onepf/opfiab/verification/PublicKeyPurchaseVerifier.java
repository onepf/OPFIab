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

package org.onepf.opfiab.verification;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import org.onepf.opfiab.model.billing.Purchase;
import org.onepf.opfutils.OPFLog;

public abstract class PublicKeyPurchaseVerifier implements PurchaseVerifier {

    @NonNull
    protected abstract String getPublicKey();

    @Nullable
    protected abstract String getData(@NonNull final Purchase purchase);

    @Nullable
    protected abstract String getSignature(@NonNull final Purchase purchase);

    @NonNull
    protected final VerificationResult verify(@Nullable final String data,
                                              @Nullable final String signature) {
        if (TextUtils.isEmpty(data) || TextUtils.isEmpty(signature)) {
            OPFLog.e("Either data or signature is empty.");
            return VerificationResult.ERROR;
        }
        //TODO
        return VerificationResult.SUCCESS;
    }

    @NonNull
    @Override
    public VerificationResult verify(@NonNull final Purchase purchase) {
        return verify(getData(purchase), getSignature(purchase));
    }
}
