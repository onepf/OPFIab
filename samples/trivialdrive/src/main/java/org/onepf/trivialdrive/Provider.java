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

package org.onepf.trivialdrive;

import android.support.annotation.StringRes;

import org.onepf.opfiab.amazon.AmazonBillingProvider;
import org.onepf.opfiab.google.GoogleBillingProvider;
import org.onepf.opfiab.openstore.ApplandBillingProvider;
import org.onepf.opfiab.openstore.AptoideBillingProvider;
import org.onepf.opfiab.openstore.SlideMEBillingProvider;
import org.onepf.opfiab.openstore.YandexBillingProvider;
import org.onepf.opfiab.samsung.SamsungBillingProvider;

public enum Provider {
    AMAZON(R.string.name_amazon, AmazonBillingProvider.NAME),
    GOOGLE(R.string.name_google, GoogleBillingProvider.NAME),
    SAMSUNG(R.string.name_samsung, SamsungBillingProvider.NAME),
    YANDEX(R.string.name_yandex, YandexBillingProvider.NAME),
    APPLAND(R.string.name_appland, ApplandBillingProvider.NAME),
    APTOIDE(R.string.name_aptoide, AptoideBillingProvider.NAME),
    SLIDEME(R.string.name_slideme, SlideMEBillingProvider.NAME),
    OPENSTORE(R.string.name_openstore, "Open Store");


    public static Provider getByName(final String name) {
        for (final Provider provider : values()) {
            if (provider.name.equals(name)) {
                return provider;
            }
        }
        return null;
    }

    private final String name;
    @StringRes
    private final int nameId;

    Provider(final int nameId, final String name) {
        this.nameId = nameId;
        this.name = name;
    }

    public int getNameId() {
        return nameId;
    }
}
