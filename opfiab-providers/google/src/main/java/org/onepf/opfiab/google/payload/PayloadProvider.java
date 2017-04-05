/*
 * Copyright 2012-2017 One Platform Foundation
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

package org.onepf.opfiab.google.payload;

import android.support.annotation.NonNull;

import org.onepf.opfiab.billing.BillingProvider;
import org.onepf.opfiab.model.billing.Purchase;

/**
 * Interface intended to provide {@link BillingProvider} specific developerPayload throw {@link Purchase}.
 */
public interface PayloadProvider {

    /**
     * Default implementation of {@link PayloadProvider}.
     * <p>
     * Using for getting custom purchase developerPayload
     */
    @NonNull
    PayloadProvider DEFAULT = new PayloadProvider() {
        @NonNull
        @Override
        public String providePayload(@NonNull String sku) {
            return "";
        }
    };

    /**
     * ets {@link BillingProvider} specific developerPayload value.
     * @param sku sku of purchase
     * @return developerPayload
     */
    @NonNull
    String providePayload(String sku);

}
