package org.onepf.opfiab.samsung.model;

import android.support.annotation.NonNull;

import org.json.JSONException;
import org.onepf.opfiab.model.JsonModel;
import org.onepf.opfiab.samsung.BillingMode;
import org.onepf.opfutils.OPFLog;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SamsungVerification extends JsonModel {

    private static final String KEY_ITEM_ID = "itemId";
    private static final String KEY_ITEM_NAME = "itemName";
    private static final String KEY_ITEM_DESC = "itemDesc";
    private static final String KEY_PURCHASE_DATE = "purchaseDate";
    private static final String KEY_PAYMENT_ID = "paymentId";
    private static final String KEY_PAYMENT_AMOUNT = "paymentAmount";
    private static final String KEY_STATUS = "status";
    private static final String KEY_MODE = "mode";

    private static final String MODE_TEST = "TEST";
    private static final String MODE_REAL = "REAL";

    private static final ThreadLocal<DateFormat> DATE_FORMAT = new ThreadLocal<DateFormat>() {
        @Override
        protected DateFormat initialValue() {
            return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        }
    };


    @NonNull
    private final String itemId;
    @NonNull
    private final String name;
    @NonNull
    private final String description;
    @NonNull
    private final Date purchaseDate;
    @NonNull
    private final String paymentId;
    @NonNull
    private final String paymentAmount;
    @NonNull
    private final BillingMode mode;
    private final boolean status;

    public SamsungVerification(@NonNull final String originalJson) throws JSONException {
        super(originalJson);
        this.itemId = jsonObject.getString(KEY_ITEM_ID);
        this.name = jsonObject.getString(KEY_ITEM_NAME);
        this.description = jsonObject.getString(KEY_ITEM_DESC);
        this.paymentId = jsonObject.getString(KEY_PAYMENT_ID);
        this.paymentAmount = jsonObject.getString(KEY_PAYMENT_AMOUNT);
        this.status = jsonObject.getBoolean(KEY_STATUS);

        final String mode = jsonObject.getString(KEY_MODE);
        switch (mode) {
            case MODE_TEST:
                this.mode = BillingMode.TEST_SUCCESS;
                break;
            case MODE_REAL:
                this.mode = BillingMode.PRODUCTION;
                break;
            default:
                throw new JSONException("Invalid billing mode: " + mode);
        }

        final String dateString = jsonObject.getString(KEY_PURCHASE_DATE);
        final Date date;
        try {
            date = DATE_FORMAT.get().parse(dateString);
        } catch (ParseException exception) {
            OPFLog.e("", exception);
            throw new JSONException("Invalid purchase date: " + dateString);
        }
        this.purchaseDate = date;
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
    public Date getPurchaseDate() {
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
    public BillingMode getMode() {
        return mode;
    }

    public boolean idStatus() {
        return status;
    }
}
