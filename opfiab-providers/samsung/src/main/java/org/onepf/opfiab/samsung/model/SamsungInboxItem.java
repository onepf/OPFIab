package org.onepf.opfiab.samsung.model;

import android.support.annotation.NonNull;

import org.json.JSONException;
import org.onepf.opfiab.samsung.SamsungUtils;

public class SamsungInboxItem extends SamsungBillingModel {

    private static final String KEY_TYPE = "mType";
    private static final String KEY_SUBSCRIPTION_END_DATE = "mSubscriptionEndDate";


    @NonNull
    private final ItemType itemType;
    @NonNull
    private final String subscriptionEndDate;

    public SamsungInboxItem(@NonNull final String originalJson) throws JSONException {
        super(originalJson);
        this.subscriptionEndDate = jsonObject.getString(KEY_SUBSCRIPTION_END_DATE);
        this.itemType = SamsungUtils.getItemType(jsonObject);
    }

    @NonNull
    public ItemType getItemType() {
        return itemType;
    }

    @NonNull
    public String getSubscriptionEndDate() {
        return subscriptionEndDate;
    }
}
