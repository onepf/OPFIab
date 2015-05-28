package org.onepf.opfiab.samsung.model;

import android.support.annotation.NonNull;

import org.json.JSONException;

public class SamsungPurchase extends SamsungBillingModel {

    private static final String KEY_PURCHASE_ID = "mPurchaseId";
    private static final String KEY_VERIFY_URL = "mVerifyUrl";


    @NonNull
    private final String purchaseId;
    @NonNull
    private final String verifyUrl;

    public SamsungPurchase(@NonNull final String originalJson) throws JSONException {
        super(originalJson);
        this.purchaseId = jsonObject.getString(KEY_PURCHASE_ID);
        this.verifyUrl = jsonObject.getString(KEY_VERIFY_URL);
    }

    @NonNull
    public String getPurchaseId() {
        return purchaseId;
    }

    @NonNull
    public String getVerifyUrl() {
        return verifyUrl;
    }
}
