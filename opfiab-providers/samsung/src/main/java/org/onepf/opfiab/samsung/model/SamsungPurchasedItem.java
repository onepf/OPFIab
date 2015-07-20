package org.onepf.opfiab.samsung.model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.json.JSONException;
import org.onepf.opfiab.samsung.SamsungUtils;

import java.util.Date;

public class SamsungPurchasedItem extends SamsungBillingModel {

    protected static final String KEY_SUBSCRIPTION_END_DATE = "mSubscriptionEndDate";


    @NonNull
    protected final ItemType itemType;
    @Nullable
    protected final Date subscriptionEndDate;

    public SamsungPurchasedItem(@NonNull final String originalJson) throws JSONException {
        super(originalJson);
        this.itemType = SamsungUtils.getItemType(jsonObject);

        final String dateString = jsonObject.optString(KEY_SUBSCRIPTION_END_DATE);
        this.subscriptionEndDate = SamsungUtils.parseDate(dateString);
    }

    @NonNull
    public ItemType getItemType() {
        return itemType;
    }

    @Nullable
    public Date getSubscriptionEndDate() {
        return subscriptionEndDate;
    }
}
