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

import android.annotation.TargetApi;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public class FragmentIabHelper extends AdvancedIabHelperAdapter {

    FragmentIabHelper(@Nullable final android.support.v4.app.Fragment supportFragment,
                      @Nullable final android.app.Fragment fragment) {
        super(new FragmentIabHelperInternal(supportFragment, fragment));
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    FragmentIabHelper(@NonNull final android.support.v4.app.Fragment supportFragment) {
        this(supportFragment, null);
    }

    FragmentIabHelper(@NonNull final android.app.Fragment fragment) {
        this(null, fragment);
    }

    @Override
    public void purchase(@NonNull final String sku) {
        advancedIabHelper.purchase(sku);
    }
}
