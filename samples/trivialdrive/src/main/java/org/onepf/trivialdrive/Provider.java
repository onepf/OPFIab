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
import org.onepf.opfiab.model.BillingProviderInfo;
import org.onepf.trivialdrive.R;

public enum Provider {
    AMAZON(R.string.name_amazon, AmazonBillingProvider.INFO),
    GOOGLE(R.string.name_google, GoogleBillingProvider.INFO),;

    public static Provider getByInfo(final BillingProviderInfo info) {
        for (final Provider provider : values()) {
            if (provider.info.equals(info)) {
                return provider;
            }
        }
        return null;
    }

    private final BillingProviderInfo info;
    @StringRes
    private final int nameId;

    Provider(final int nameId, final BillingProviderInfo info) {
        this.nameId = nameId;
        this.info = info;
    }

    public int getNameId() {
        return nameId;
    }
}
