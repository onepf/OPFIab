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

package org.onepf.opfiab.samsung.model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.onepf.opfiab.model.billing.SkuType;

public enum ItemType {

    CONSUMABLE("00"),
    NON_CONSUMABLE("01"),
    SUBSCRIPTION("02"),
    ALL("10");

    @Nullable
    public static ItemType fromCode(@NonNull final String code) {
        for (final ItemType itemType : values()) {
            if (itemType.code.equals(code)) {
                return itemType;
            }
        }
        return null;
    }

    @NonNull
    public static ItemType fromSkuType(@NonNull final SkuType type) {
        switch (type) {
            case CONSUMABLE:
                return CONSUMABLE;
            case ENTITLEMENT:
                return NON_CONSUMABLE;
            case SUBSCRIPTION:
                return SUBSCRIPTION;
            default:
                throw new IllegalArgumentException("Can't convert SkyType: " + type);
        }

    }

    @NonNull
    private final String code;

    ItemType(@NonNull final String code) {
        this.code = code;
    }

    @NonNull
    public String getCode() {
        return code;
    }
}
