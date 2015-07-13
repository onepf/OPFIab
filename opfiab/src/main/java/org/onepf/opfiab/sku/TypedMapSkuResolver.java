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

package org.onepf.opfiab.sku;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import org.onepf.opfiab.model.billing.SkuType;

import java.util.HashMap;
import java.util.Map;

/**
 * Simple map-based implementation of {@link TypedSkuResolver} interface.
 */
public class TypedMapSkuResolver implements TypedSkuResolver {

    protected final MapSkuResolver mapSkuResolver = new MapSkuResolver();
    protected final Map<String, SkuType> types = new HashMap<>();

    public TypedMapSkuResolver() {
        super();
    }

    /**
     * Adds SKU mapping with corresponding SKU type.
     *
     * @param sku         Original SKU.
     * @param resolvedSku Provider specific SKU. Can be null if there's no need in mapping.
     * @param skuType     Type of the mapped SKU.
     */
    public void add(@NonNull final String sku,
                    @Nullable final String resolvedSku,
                    @NonNull final SkuType skuType) {
        types.put(sku, skuType);
        if (!TextUtils.isEmpty(resolvedSku)) {
            types.put(resolvedSku, skuType);
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
        if (!types.containsKey(sku)) {
            return SkuType.UNKNOWN;
        }
        return types.get(sku);
    }
}
