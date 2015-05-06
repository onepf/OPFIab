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

/**
 * Simple implementation of {@link GooglePurchaseVerifier} that store public key in insecure way.
 * <p>
 * It's strongly recommended to make your own implementation that doesn't store key as a plain
 * string.
 */
public class SimpleGooglePurchaseVerifier extends GooglePurchaseVerifier {

    @NonNull
    private final String publicKey;

    public SimpleGooglePurchaseVerifier(@NonNull final String publicKey) {
        super();
        this.publicKey = publicKey;
    }

    @NonNull
    @Override
    protected String getPublicKey() {
        return publicKey;
    }
}
