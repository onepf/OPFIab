package org.onepf.opfiab.samsung.model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

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

    protected static final String KEY_ITEM_ID = "itemId";
    protected static final String KEY_ITEM_NAME = "itemName";
    protected static final String KEY_ITEM_DESC = "itemDesc";
    protected static final String KEY_PURCHASE_DATE = "purchaseDate";
    protected static final String KEY_PAYMENT_ID = "paymentId";
    protected static final String KEY_PAYMENT_AMOUNT = "paymentAmount";
    protected static final String KEY_STATUS = "status";
    protected static final String KEY_MODE = "mode";

    protected static final String MODE_TEST = "TEST";
    protected static final String MODE_REAL = "REAL";

    protected static final ThreadLocal<DateFormat> DATE_FORMAT = new ThreadLocal<DateFormat>() {
        @Override
        protected DateFormat initialValue() {
            return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        }
    };


    protected final boolean status;
    @Nullable
    protected final String itemId;
    @Nullable
    protected final String name;
    @Nullable
    protected final String description;
    @Nullable
    protected final Date purchaseDate;
    @Nullable
    protected final String paymentId;
    @Nullable
    protected final String paymentAmount;
    @Nullable
    protected final BillingMode mode;

    @SuppressWarnings("PMD.PreserveStackTrace")
    public SamsungVerification(@NonNull final String originalJson) throws JSONException {
        super(originalJson);
        this.status = jsonObject.getBoolean(KEY_STATUS);
        this.itemId = jsonObject.optString(KEY_ITEM_ID);
        this.name = jsonObject.optString(KEY_ITEM_NAME);
        this.description = jsonObject.optString(KEY_ITEM_DESC);
        this.paymentId = jsonObject.optString(KEY_PAYMENT_ID);
        this.paymentAmount = jsonObject.optString(KEY_PAYMENT_AMOUNT);

        final String mode = jsonObject.optString(KEY_MODE);
        if (mode == null) {
            this.mode = null;
        } else if (mode.equals(MODE_TEST)) {
            this.mode = BillingMode.TEST_SUCCESS;
        } else if (mode.equals(MODE_REAL)) {
            this.mode = BillingMode.PRODUCTION;
        } else {
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

    @Nullable
    public String getItemId() {
        return itemId;
    }

    @Nullable
    public String getName() {
        return name;
    }

    @Nullable
    public String getDescription() {
        return description;
    }

    @Nullable
    public Date getPurchaseDate() {
        return purchaseDate;
    }

    @Nullable
    public String getPaymentId() {
        return paymentId;
    }

    @Nullable
    public String getPaymentAmount() {
        return paymentAmount;
    }

    @Nullable
    public BillingMode getMode() {
        return mode;
    }

    public boolean isStatus() {
        return status;
    }
}
