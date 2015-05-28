package org.onepf.opfiab.samsung;

import android.support.annotation.NonNull;

import org.onepf.opfiab.sku.MapSkuResolver;

public class SamsungMapSkuResolver extends MapSkuResolver implements SamsungSkuResolver {

    @NonNull
    private final String groupId;

    public SamsungMapSkuResolver(@NonNull final String groupId) {
        this.groupId = groupId;
    }

    @NonNull
    @Override
    public String getGroupId() {
        return groupId;
    }
}
