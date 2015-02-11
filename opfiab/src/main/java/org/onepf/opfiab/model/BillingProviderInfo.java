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

package org.onepf.opfiab.model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public final class BillingProviderInfo {

    @NonNull
    private final String name;

    @Nullable
    private final String packageName;

    public BillingProviderInfo(@NonNull final String name, @Nullable final String packageName) {
        this.name = name;
        this.packageName = packageName;
    }

    @NonNull
    public String getName() {
        return name;
    }

    @Nullable
    public String getPackageName() {
        return packageName;
    }

    //CHECKSTYLE:OFF
    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof BillingProviderInfo)) return false;

        final BillingProviderInfo that = (BillingProviderInfo) o;

        if (!name.equals(that.name)) return false;
        //noinspection RedundantIfStatement
        if (packageName != null ? !packageName.equals(that.packageName) : that.packageName != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + (packageName != null ? packageName.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        //TODO
        return String.format("{\"name\":\"%s\", \"packageName\":\"%s\"}", name,
                             String.valueOf(packageName));
    }
    //CHECKSTYLE:ON
}
