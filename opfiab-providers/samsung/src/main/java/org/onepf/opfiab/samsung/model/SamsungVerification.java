package org.onepf.opfiab.samsung.model;

import android.support.annotation.NonNull;

import org.json.JSONException;

public class SamsungVerification extends BillingModel {

    private static final String KEY_ITEM_ID ="itemId";
    private static final String KEY_ITEM_NAME = "itemName";
    private static final String KEY_ITEM_DESC = "itemDesc";
    private static final String KEY_PURCHASE_DATE = "purchaseDate";
    private static final String KEY_PAYMENT_ID = "paymentId";
    private static final String KEY_PAYMENT_AMOUNT = "paymentAmount";
    private static final String KEY_STATUS = "status";


    @NonNull
    private final String itemId;
    @NonNull
    private final String name;
    @NonNull
    private final String description;
    @NonNull
    private final String purchaseDate;
    @NonNull
    private final String paymentId;
    @NonNull
    private final String paymentAmount;
    @NonNull
    private final String status;

    public SamsungVerification(@NonNull final String originalJson) throws JSONException {
        super(originalJson);
        this.itemId = jsonObject.getString(KEY_ITEM_ID);
        this.name = jsonObject.getString(KEY_ITEM_NAME);
        this.description = jsonObject.getString(KEY_ITEM_DESC);
        this.purchaseDate = jsonObject.getString(KEY_PURCHASE_DATE);
        this.paymentId = jsonObject.getString(KEY_PAYMENT_ID);
        this.paymentAmount = jsonObject.getString(KEY_PAYMENT_AMOUNT);
        this.status = jsonObject.getString(KEY_STATUS);
    }

    @NonNull
    public String getItemId() {
        return itemId;
    }

    @NonNull
    public String getName() {
        return name;
    }

    @NonNull
    public String getDescription() {
        return description;
    }

    @NonNull
    public String getPurchaseDate() {
        return purchaseDate;
    }

    @NonNull
    public String getPaymentId() {
        return paymentId;
    }

    @NonNull
    public String getPaymentAmount() {
        return paymentAmount;
    }

    @NonNull
    public String getStatus() {
        return status;
    }
}
