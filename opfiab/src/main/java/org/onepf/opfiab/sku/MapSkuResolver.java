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
import android.text.TextUtils;

import java.util.HashMap;
import java.util.Map;


/**
 * Simple map-based implementation of {@link SkuResolver} interface.
 */
public class MapSkuResolver implements SkuResolver {

    protected final Map<String, String> direct = new HashMap<>();
    protected final Map<String, String> reverse = new HashMap<>();

    public MapSkuResolver() {
        super();
    }

    /**
     * Adds SKU mapping.
     *
     * @param sku         Original SKU value.
     * @param resolvedSku SKU value to which original one should be mapped.
     */
    public void add(@NonNull final String sku, @NonNull final String resolvedSku) {
        if (!TextUtils.equals(sku, resolvedSku)) {
            direct.put(sku, resolvedSku);
            reverse.put(resolvedSku, sku);
        }
    }

    @NonNull
    @Override
    public String resolve(@NonNull final String sku) {
        return direct.containsKey(sku)
                ? direct.get(sku)
                : DEFAULT.resolve(sku);
    }

    @NonNull
    @Override
    public String revert(@NonNull final String resolvedSku) {
        return reverse.containsKey(resolvedSku)
                ? reverse.get(resolvedSku)
                : DEFAULT.revert(resolvedSku);
    }
}
