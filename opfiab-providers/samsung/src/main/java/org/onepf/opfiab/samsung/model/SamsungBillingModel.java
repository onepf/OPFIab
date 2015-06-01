package org.onepf.opfiab.samsung.model;

import android.support.annotation.NonNull;

import org.json.JSONException;
import org.onepf.opfiab.samsung.SamsungUtils;

import java.util.Date;

abstract class SamsungBillingModel extends SamsungModel {

    private static final String KEY_PAYMENT_ID = "mPaymentId";
    private static final String KEY_PURCHASE_DATE = "mPurchaseDate";


    @NonNull
    private final String paymentId;
    @NonNull
    private final Date purchaseDate;

    public SamsungBillingModel(@NonNull final String originalJson) throws JSONException {
        super(originalJson);
        this.paymentId = jsonObject.getString(KEY_PAYMENT_ID);

        final String dateString = jsonObject.getString(KEY_PURCHASE_DATE);
        final Date date = SamsungUtils.parseDate(dateString);
        if (date == null) {
            throw new JSONException("Invalid purchase date: " + dateString);
        }
        this.purchaseDate = date;
    }

    @NonNull
    public String getPaymentId() {
        return paymentId;
    }

    @NonNull
    public Date getPurchaseDate() {
        return purchaseDate;
    }
}
