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

package org.onepf.opfiab.model.event.billing;

import android.support.annotation.NonNull;

import org.onepf.opfiab.billing.BillingProvider;

/**
 * Model class representing request of some action from {@link BillingProvider}.
 * <br>
 * Please not that not every request will cause corresponding {@link BillingResponse}, different
 * {@link BillingProvider}s can behave differently.
 */
public abstract class BillingRequest extends BillingEvent {

    protected BillingRequest(@NonNull final Type type) {
        super(type);
    }
}
