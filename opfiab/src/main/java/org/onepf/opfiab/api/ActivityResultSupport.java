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
import android.support.annotation.Nullable;

interface ActivityResultSupport {

    /**
     * Notifies library about received activity result.
     * <p>
     * Intended to be called from {@link Activity} used for
     * {@link SimpleIabHelper#purchase(Activity, String)}.
     *
     * @param activity Activity object which received
     *                 {@link Activity#onActivityResult(int, int, Intent)} call.
     */
    void onActivityResult(@NonNull final Activity activity, final int requestCode,
                          final int resultCode, @Nullable final Intent data);
}
