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

package org.onepf.opfiab;

import android.support.annotation.NonNull;

import java.util.Arrays;

import de.greenrobot.event.EventBus;

public abstract class IabHelper implements BillingBase {

    @NonNull
    protected final EventBus eventBus = OPFIab.getEventBus();

    public final void skuDetails(@NonNull final String... skus) {
        skuDetails(Arrays.asList(skus));
    }

    public abstract void setup();
}
