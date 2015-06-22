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
            throw new IllegalArgumentException("No type for SKU: " + sku);
        }
        return types.get(sku);
    }
}
