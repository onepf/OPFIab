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

package org.onepf.opfiab.model.event.billing;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;
import org.onepf.opfiab.billing.BillingProvider;
import org.onepf.opfiab.model.billing.Purchase;
import org.onepf.opfiab.verification.PurchaseVerifier;
import org.onepf.opfiab.verification.VerificationResult;
import org.onepf.opfutils.OPFLog;

import static org.json.JSONObject.NULL;

/**
 * Response from {@link BillingProvider} for corresponding {@link PurchaseRequest}.
 */
public class PurchaseResponse extends BillingResponse {

    private static final String NAME_PURCHASE = "purchase";
    private static final String NAME_VERIFICATION_RESULT = "verification_result";


    @Nullable
    private final Purchase purchase;
    @Nullable
    private final VerificationResult verificationResult;

    public PurchaseResponse(@NonNull final Status status,
                            @Nullable final String providerName,
                            @Nullable final Purchase purchase,
                            @Nullable final VerificationResult verificationResult) {
        super(BillingEventType.PURCHASE, status, providerName);
        this.purchase = purchase;
        this.verificationResult = verificationResult;
    }

    public PurchaseResponse(@NonNull final Status status,
                            @Nullable final String providerName,
                            @Nullable final Purchase purchase) {
        this(status, providerName, purchase, null);
    }

    public PurchaseResponse(@NonNull final Status status,
                            @Nullable final String providerName) {
        this(status, providerName, null);
    }

    /**
     * Gets purchase acquired by user with corresponding {@link BillingRequest}.
     *
     * @return Purchase object. Can be null.
     *
     * @see #isSuccessful()
     */
    @Nullable
    public Purchase getPurchase() {
        return purchase;
    }

    /**
     * Gets verification result for corresponding purchase.
     *
     * @return VerificationResult. Can be null.
     *
     * @see #isSuccessful()
     * @see PurchaseVerifier
     */
    @Nullable
    public VerificationResult getVerificationResult() {
        return verificationResult;
    }

    /**
     * @return True if purchase was successful and corresponding purchase object is successfully
     * verified, false otherwise.
     */
    @Override
    public boolean isSuccessful() {
        return super.isSuccessful() && verificationResult == VerificationResult.SUCCESS;
    }

    @NonNull
    @Override
    public JSONObject toJson() {
        final JSONObject jsonObject = super.toJson();
        try {
            jsonObject.put(NAME_PURCHASE, purchase == null ? NULL : purchase.toJson());
            jsonObject.put(NAME_VERIFICATION_RESULT,
                    verificationResult == null ? NULL : verificationResult);
        } catch (JSONException exception) {
            OPFLog.e("", exception);
        }
        return jsonObject;
    }
}
