package org.onepf.opfiab.samsung.model;

import android.support.annotation.NonNull;

import org.json.JSONException;
import org.onepf.opfiab.samsung.SamsungUtils;

import java.util.Date;

public abstract class SamsungBillingModel extends SamsungModel {

    protected static final String KEY_PURCHASE_ID = "mPurchaseId";
    protected static final String KEY_PAYMENT_ID = "mPaymentId";
    protected static final String KEY_PURCHASE_DATE = "mPurchaseDate";

    @NonNull
    protected final String purchaseId;
    @NonNull
    protected final String paymentId;
    @NonNull
    protected final Date purchaseDate;

    public SamsungBillingModel(@NonNull final String originalJson) throws JSONException {
        super(originalJson);
        this.purchaseId = jsonObject.getString(KEY_PURCHASE_ID);
        this.paymentId = jsonObject.getString(KEY_PAYMENT_ID);

        final String dateString = jsonObject.getString(KEY_PURCHASE_DATE);
        final Date date = SamsungUtils.parseDate(dateString);
        if (date == null) {
            throw new JSONException("Invalid purchase date: " + dateString);
        }
        this.purchaseDate = date;
    }

    @NonNull
    public String getPurchaseId() {
        return purchaseId;
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
