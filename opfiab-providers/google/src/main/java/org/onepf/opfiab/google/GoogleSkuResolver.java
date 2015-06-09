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

import org.onepf.opfiab.model.billing.SkuType;
import org.onepf.opfiab.sku.SkuResolver;

/**
 * This Google specific {@link SkuResolver} must be used with {@link GoogleBillingProvider} since
 * it's necessary to be able to resolve SKU type.
 */
public interface GoogleSkuResolver extends SkuResolver {

    /**
     * Resolves type of supplied SKU. SKU should not yet be resolved.
     *
     * @param sku SKU to resolve type for.
     *
     * @return SKU type, can't be null.
     */
    @NonNull
    SkuType resolveType(@NonNull final String sku);
}
