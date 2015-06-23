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
import android.support.annotation.NonNull;

import org.json.JSONException;
import org.onepf.opfiab.model.billing.Purchase;
import org.onepf.opfiab.samsung.model.SamsungVerification;
import org.onepf.opfiab.util.OPFIabUtils;
import org.onepf.opfiab.verification.PurchaseVerifier;
import org.onepf.opfiab.verification.VerificationResult;
import org.onepf.opfutils.OPFLog;
import org.onepf.opfutils.OPFUtils;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class SamsungPurchaseVerifier implements PurchaseVerifier {

    protected static final String VERIFY_URL
            = "https://iap.samsungapps.com/iap/appsItemVerifyIAPReceipt.as?protocolVersion=2.0"
            + "&purchaseID=";


    @NonNull
    protected final Context context;
    @NonNull
    protected final BillingMode billingMode;

    public SamsungPurchaseVerifier(@NonNull final Context context,
                                   @NonNull final BillingMode billingMode) {
        this.context = context;
        this.billingMode = billingMode;
    }

    @NonNull
    @Override
    public VerificationResult verify(@NonNull final Purchase purchase) {
        if (!OPFUtils.isConnected(context)) {
            OPFLog.e("Can't verify purchase, no connection.");
            return VerificationResult.ERROR;
        }
        try {
            final HttpURLConnection connection = (HttpURLConnection)
                    new URL(VERIFY_URL + purchase.getToken()).openConnection();
            connection.connect();
            final int responseCode = connection.getResponseCode();
            OPFLog.d("Verify response code: " + responseCode);
            if (responseCode != HttpURLConnection.HTTP_OK) {
                return VerificationResult.ERROR;
            }
            final String body = OPFIabUtils.toString(connection.getInputStream());
            final SamsungVerification verification = new SamsungVerification(body);
            return verification.isStatus()  && verification.getMode() == this.billingMode
                    ? VerificationResult.SUCCESS : VerificationResult.FAILED;
        } catch (IOException | JSONException exception) {
            OPFLog.e("", exception);
        }
        return VerificationResult.ERROR;
    }
}
