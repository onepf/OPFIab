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

package org.onepf.opfiab.yandex;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;

import org.onepf.opfiab.openstore.OpenStoreBillingHelper;

public class YandexBillingHelper extends OpenStoreBillingHelper {

    protected static final String PACKAGE = "com.yandex.store";


    public YandexBillingHelper(final Context context) {
        super(context);
    }

    @NonNull
    @Override
    protected Intent getOpenAppstoreServiceIntent() {
        final Intent intent = new Intent(ACTION_BIND_OPENSTORE);
        intent.setPackage(PACKAGE);
        return intent;
    }
}
