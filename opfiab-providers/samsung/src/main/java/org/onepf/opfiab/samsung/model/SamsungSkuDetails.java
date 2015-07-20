package org.onepf.opfiab.samsung.model;

import android.support.annotation.NonNull;

import org.json.JSONException;
import org.onepf.opfiab.samsung.SamsungUtils;

public class SamsungSkuDetails extends SamsungModel {

    protected static final String KEY_SUBSCRIPTION_UNIT = "mSubscriptionDurationUnit";
    protected static final String KEY_SUBSCRIPTION_MULTIPLIER = "mSubscriptionDurationMultiplier";


    @NonNull
    protected final ItemType itemType;
    @NonNull
    protected final String subscriptionUnit;
    @NonNull
    protected final String subscriptionMultiplier;

    public SamsungSkuDetails(@NonNull final String originalJson) throws JSONException {
        super(originalJson);
        this.subscriptionUnit = jsonObject.getString(KEY_SUBSCRIPTION_UNIT);
        this.subscriptionMultiplier = jsonObject.getString(KEY_SUBSCRIPTION_MULTIPLIER);
        this.itemType = SamsungUtils.getItemType(jsonObject);
    }

    @NonNull
    public ItemType getItemType() {
        return itemType;
    }

    @NonNull
    public String getSubscriptionUnit() {
        return subscriptionUnit;
    }

    @NonNull
    public String getSubscriptionMultiplier() {
        return subscriptionMultiplier;
    }
}
