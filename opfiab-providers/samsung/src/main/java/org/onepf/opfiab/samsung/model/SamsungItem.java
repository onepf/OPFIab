package org.onepf.opfiab.samsung.model;

import android.support.annotation.NonNull;

import org.json.JSONException;
import org.onepf.opfiab.samsung.SamsungUtils;

public class SamsungItem extends SamsungModel {

    private static final String KEY_SUBSCRIPTION_UNIT = "mSubscriptionDurationUnit";
    private static final String KEY_SUBSCRIPTION_MULTIPLIER = "mSubscriptionDurationMultiplier";


    @NonNull
    private final ItemType itemType;
    @NonNull
    private final String subscriptionUnit;
    @NonNull
    private final String subscriptionMultiplier;

    public SamsungItem(@NonNull final String originalJson) throws JSONException {
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
