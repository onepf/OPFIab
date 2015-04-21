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

import org.onepf.opfiab.model.billing.Purchase;

/**
 * Interface intended to define purchase verification algorithm.
 * <br>
 * Verification process is supposed to confirm the fact that user acquired purchase legitimately.
 * <br>
 * Typically some cryptography checks or external server requests.
 */
public interface PurchaseVerifier {

    /**
     * Default implementation of {@link PurchaseVerifier} which always return
     * {@link VerificationResult#SUCCESS} from {@link #verify(Purchase)} call.
     */
    @NonNull
    PurchaseVerifier DEFAULT = new PurchaseVerifier() {

        @NonNull
        @Override
        public VerificationResult verify(@NonNull final Purchase purchase) {
            return VerificationResult.SUCCESS;
        }
    };

    /**
     * Attempt to verify purchase owned by user.
     * <br>
     * Intended to be called from background thread, may perform lengthy operations.
     *
     * @param purchase Purchase object to verify.
     * @return Verification result.
     */
    @NonNull
    VerificationResult verify(@NonNull final Purchase purchase);
}
