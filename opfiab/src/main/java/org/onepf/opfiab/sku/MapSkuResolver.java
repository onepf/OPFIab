/*
 * Copyright 2012-2014 One Platform Foundation
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

import java.util.HashMap;
import java.util.Map;


public class MapSkuResolver implements SkuResolver {

    @NonNull
    protected final Map<String, String> direct = new HashMap<>();
    @NonNull
    protected final Map<String, String> reverse = new HashMap<>();

    public MapSkuResolver() {
        super();
    }

    public MapSkuResolver(@NonNull final Map<String, String> map) {
        this();
        add(map);
    }

    public void add(@NonNull final Map<String, String> map) {
        direct.putAll(map);
        for (final Map.Entry<String, String> entry : map.entrySet()) {
            final String key = entry.getKey();
            final String value = entry.getValue();
            if (value == null) {
                throw new IllegalArgumentException("Mapped sku can't be null.");
            }
            reverse.put(value, key);
        }
    }

    public void add(@NonNull final String sku, @NonNull final String resolvedSku) {
        direct.put(sku, resolvedSku);
        reverse.put(resolvedSku, sku);
    }

    @NonNull
    @Override
    public String resolve(@NonNull final String sku) {
        return direct.containsKey(sku)
                ? direct.get(sku)
                : STUB.resolve(sku);
    }

    @NonNull
    @Override
    public String revert(@NonNull final String resolvedSku) {
        return reverse.containsKey(resolvedSku)
                ? reverse.get(resolvedSku)
                : STUB.revert(resolvedSku);
    }
}
