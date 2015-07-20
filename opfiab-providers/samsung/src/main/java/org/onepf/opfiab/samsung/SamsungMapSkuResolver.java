package org.onepf.opfiab.samsung;

import android.support.annotation.NonNull;

import org.onepf.opfiab.sku.TypedMapSkuResolver;

public class SamsungMapSkuResolver extends TypedMapSkuResolver implements SamsungSkuResolver {

    @NonNull
    protected final String groupId;

    public SamsungMapSkuResolver(@NonNull final String groupId) {
        super();
        this.groupId = groupId;
    }

    @NonNull
    @Override
    public String getGroupId() {
        return groupId;
    }
}
