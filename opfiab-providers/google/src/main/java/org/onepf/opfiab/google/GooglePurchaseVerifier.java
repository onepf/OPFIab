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

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.onepf.opfiab.google.model.SignedPurchase;
import org.onepf.opfiab.model.billing.Purchase;
import org.onepf.opfiab.verification.PublicKeyPurchaseVerifier;
import org.onepf.opfiab.verification.PurchaseVerifier;

/**
 * This {@link PurchaseVerifier} checks signatures on Google purchases in order to verify them.
 */
public abstract class GooglePurchaseVerifier extends PublicKeyPurchaseVerifier {

    @Nullable
    @Override
    protected String getData(@NonNull final Purchase purchase) {
        return purchase.getOriginalJson();
    }

    @Nullable
    @Override
    protected String getSignature(@NonNull final Purchase purchase) {
        if (purchase instanceof SignedPurchase) {
            final SignedPurchase signedPurchase = (SignedPurchase) purchase;
            return signedPurchase.getSignature();
        }
        return null;
    }
}
