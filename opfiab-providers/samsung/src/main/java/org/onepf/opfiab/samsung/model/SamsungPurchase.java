package org.onepf.opfiab.samsung.model;

import android.support.annotation.NonNull;

import org.json.JSONException;

public class SamsungPurchase extends SamsungBillingModel {

    private static final String KEY_VERIFY_URL = "mVerifyUrl";


    @NonNull
    private final String verifyUrl;

    public SamsungPurchase(@NonNull final String originalJson) throws JSONException {
        super(originalJson);
        this.verifyUrl = jsonObject.getString(KEY_VERIFY_URL);
    }

    @NonNull
    public String getVerifyUrl() {
        return verifyUrl;
    }
}
