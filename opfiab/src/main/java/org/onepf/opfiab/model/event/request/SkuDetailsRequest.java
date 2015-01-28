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

package org.onepf.opfiab.model.event.request;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class SkuDetailsRequest extends Request {

    @NonNull
    private final ArrayList<String> skus;

    public SkuDetailsRequest(@NonNull final Collection<String> skus) {
        super(Type.SKU_DETAILS);
        this.skus = new ArrayList<>(skus);
    }

    @NonNull
    public List<String> getSkus() {
        return Collections.unmodifiableList(skus);
    }
}
