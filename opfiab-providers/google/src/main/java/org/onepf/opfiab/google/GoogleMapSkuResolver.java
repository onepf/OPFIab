/*
 * Copyright 2012-2015 One Platform Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.onepf.opfiab.google;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import org.onepf.opfiab.model.billing.SkuType;
import org.onepf.opfiab.sku.MapSkuResolver;

import java.util.HashMap;
import java.util.Map;

/**
 * This is a simple map-based implementation of {@link GoogleSkuResolver}.
 */
public class GoogleMapSkuResolver implements GoogleSkuResolver {

    protected final MapSkuResolver mapSkuResolver = new MapSkuResolver();
    protected final Map<String, SkuType> typeMap = new HashMap<>();

    public GoogleMapSkuResolver() {
        super();
    }

    /**
     * Adds SKU mapping with corresponding SKU type.
     *
     * @param sku         Original SKU.
     * @param resolvedSku Provider specific SKU. Can be null if there's no need for mapping.
     * @param skuType     Type of the mapped SKU.
     */
    public void add(@NonNull final String sku,
                    @Nullable final String resolvedSku,
                    @NonNull final SkuType skuType) {
        typeMap.put(sku, skuType);
        if (!TextUtils.isEmpty(resolvedSku)) {
            typeMap.put(resolvedSku, skuType);
            mapSkuResolver.add(sku, resolvedSku);
        }
    }

    /**
     * Same as {@code add(sku, null, skuType)}.
     *
     * @see #add(String, String, SkuType)
     */
    public void add(@NonNull final String sku,
                    @NonNull final SkuType skuType) {
        add(sku, null, skuType);
    }

    @NonNull
    @Override
    public String resolve(@NonNull final String sku) {
        return mapSkuResolver.resolve(sku);
    }

    @NonNull
    @Override
    public String revert(@NonNull final String resolvedSku) {
        return mapSkuResolver.revert(resolvedSku);
    }

    @NonNull
    @Override
    public SkuType resolveType(@NonNull final String sku) {
        final SkuType skuType = typeMap.get(sku);
        return skuType != null ? skuType : SkuType.UNKNOWN;
    }
}
