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

package org.onepf.opfiab.api;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;

import org.onepf.opfiab.model.billing.Purchase;

import java.util.Set;

/**
 * Version of {@link IabHelper} featuring {@link #purchase(String)} with activity.
 *
 * @see #purchase(Activity, String)
 */
public interface SimpleIabHelper extends IabHelper, ActivityResultSupport {

    /**
     * Same as {@link #purchase(String)} except passed activity <b>must</b> override
     * {@link Activity#onActivityResult(int, int, Intent)}.
     *
     * @see #onActivityResult(Activity, int, int, Intent)
     */
    void purchase(@NonNull final Activity activity, @NonNull final String sku);

    /**
     * Same as {@link #consume(Purchase)} except passed activity <b>must</b> override
     * {@link Activity#onActivityResult(int, int, Intent)}.
     *
     * @see #onActivityResult(Activity, int, int, Intent)
     */
    void consume(@NonNull final Activity activity, @NonNull final Purchase purchase);

    /**
     * Same as {@link #inventory(boolean)} except passed activity <b>must</b> override
     * {@link Activity#onActivityResult(int, int, Intent)}.
     *
     * @see #onActivityResult(Activity, int, int, Intent)
     */
    void inventory(@NonNull final Activity activity, final boolean startOver);

    /**
     * Same as {@link #skuDetails(Set)} except passed activity <b>must</b> override
     * {@link Activity#onActivityResult(int, int, Intent)}.
     *
     * @see #onActivityResult(Activity, int, int, Intent)
     */
    void skuDetails(@NonNull final Activity activity, @NonNull final Set<String> skus);

    /**
     * Same as {@link #skuDetails(Activity, Set)}.
     */
    void skuDetails(@NonNull final Activity activity, @NonNull final String... skus);
}
