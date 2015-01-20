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

package org.onepf.opfiab;

import android.support.annotation.NonNull;

import org.onepf.opfiab.sku.SkuResolver;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public final class OPFIabUtils {

    private OPFIabUtils() {
        throw new UnsupportedOperationException();
    }

    public static Set<String> resolve(@NonNull final SkuResolver resolver,
                                      @NonNull final String... skus) {
        return resolveSkus(resolver, Arrays.asList(skus));
    }

    public static Set<String> resolveSkus(@NonNull final SkuResolver resolver,
                                          @NonNull final Collection<String> skus) {
        final Set<String> resolvedSkus = new HashSet<>();
        for (final String sku : skus) {
            resolvedSkus.add(resolver.resolve(sku));
        }
        return resolvedSkus;
    }
}
