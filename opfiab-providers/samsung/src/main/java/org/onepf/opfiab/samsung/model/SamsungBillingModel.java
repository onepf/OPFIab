package org.onepf.opfiab.samsung.model;

import android.support.annotation.NonNull;

import org.json.JSONException;

abstract class SamsungBillingModel extends SamsungModel {

    private static final String KEY_PAYMENT_ID = "mPaymentId";
    private static final String KEY_PURCHASE_DATE = "mPurchaseDate";


    @NonNull
    private final String paymentId;
    @NonNull
    private final String purchaseDate;

    public SamsungBillingModel(@NonNull final String originalJson) throws JSONException {
        super(originalJson);
        this.paymentId = jsonObject.getString(KEY_PAYMENT_ID);
        this.purchaseDate = jsonObject.getString(KEY_PURCHASE_DATE);
    }

    @NonNull
    public String getPaymentId() {
        return paymentId;
    }

    @NonNull
    public String getPurchaseDate() {
        return purchaseDate;
    }
}
