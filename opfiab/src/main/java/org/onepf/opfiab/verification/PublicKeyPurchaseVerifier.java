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
import android.util.Base64;

import org.onepf.opfiab.model.billing.Purchase;
import org.onepf.opfutils.OPFLog;

import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Public key based implementation of {@link PurchaseVerifier} interface.
 * <br>
 * This class attempts to confirm that purchase data which was returned from {@link #getData(Purchase)}
 * was signed with a private key corresponding to one returned from {@link #getPublicKey()}.
 */
public abstract class PublicKeyPurchaseVerifier implements PurchaseVerifier {

    protected static final String KEY_FACTORY_ALGORITHM = "RSA";
    protected static final String SIGNATURE_ALGORITHM = "SHA1withRSA";

    @NonNull
    protected abstract String getPublicKey();

    @Nullable
    protected abstract String getData(@NonNull final Purchase purchase);

    @Nullable
    protected abstract String getSignature(@NonNull final Purchase purchase);

    @NonNull
    private PublicKey publicKey() {
        final String publicKey = getPublicKey();
        if (TextUtils.isEmpty(publicKey)) {
            throw new IllegalStateException("Public key can't be null.");
        }
        final byte[] decodedKey = Base64.decode(publicKey, Base64.DEFAULT);
        try {
            final KeyFactory keyFactory = KeyFactory.getInstance(KEY_FACTORY_ALGORITHM);
            return keyFactory.generatePublic(new X509EncodedKeySpec(decodedKey));
        } catch (NoSuchAlgorithmException | InvalidKeySpecException exception) {
            throw new IllegalStateException("Can't create PublicKey.", exception);
        }
    }

    @SuppressFBWarnings({"DM_DEFAULT_ENCODING", "MDM_STRING_BYTES_ENCODING"})
    @NonNull
    private Signature signature(@NonNull final String data) {
        try {
            final Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
            signature.initVerify(publicKey());
            signature.update(data.getBytes());
            return signature;
        } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException exception) {
            throw new IllegalStateException("Can't initialize Signature.", exception);
        }
    }

    @NonNull
    protected final VerificationResult verify(@Nullable final String data,
                                              @Nullable final String signature) {
        if (TextUtils.isEmpty(data) || TextUtils.isEmpty(signature)) {
            OPFLog.e("Either data or signature is empty.");
            return VerificationResult.ERROR;
        }
        try {
            final byte[] decodedSignature = Base64.decode(signature, Base64.DEFAULT);
            final boolean result = signature(data).verify(decodedSignature);
            return result ? VerificationResult.SUCCESS : VerificationResult.FAILED;
        } catch (SignatureException | IllegalArgumentException exception) {
            OPFLog.e("Error verifying purchase.", exception);
            return VerificationResult.ERROR;
        }
    }

    @NonNull
    @Override
    public VerificationResult verify(@NonNull final Purchase purchase) {
        return verify(getData(purchase), getSignature(purchase));
    }
}
