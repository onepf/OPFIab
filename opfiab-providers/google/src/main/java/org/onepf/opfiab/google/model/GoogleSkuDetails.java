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

package org.onepf.opfiab.google.model;

import android.support.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

public class GoogleSkuDetails extends GoogleModel {

    private static final String NAME_TYPE = "type";
    private static final String NAME_PRICE = "price";
    private static final String NAME_CURRENCY = "price_currency_code";
    private static final String NAME_TITLE = "title";
    private static final String NAME_DESCRIPTION = "description";
    private static final String NAME_MICROS = "price_amount_micros";

    @NonNull
    private final ItemType itemType;
    @NonNull
    private final String price;
    @NonNull
    private final String currency;
    @NonNull
    private final String title;
    @NonNull
    private final String description;
    private final long micros;

    public GoogleSkuDetails(@NonNull final String originalJson,
                            @NonNull final JSONObject jsonObject) throws JSONException {
        super(originalJson, jsonObject);
        final String itemTypeCode = jsonObject.getString(NAME_TYPE);
        final ItemType itemType = ItemType.fromCode(itemTypeCode);
        if (itemType == null) {
            throw new JSONException("Unrecognized itemType: " + itemTypeCode);
        }
        this.itemType = itemType;

        this.price = jsonObject.getString(NAME_PRICE);
        this.micros = jsonObject.getLong(NAME_MICROS);
        this.currency = jsonObject.getString(NAME_CURRENCY);
        this.title = jsonObject.getString(NAME_TITLE);
        this.description = jsonObject.getString(NAME_DESCRIPTION);
    }

    public GoogleSkuDetails(@NonNull final String originalJson) throws JSONException {
        this(originalJson, new JSONObject(originalJson));
    }

    @NonNull
    public ItemType getItemType() {
        return itemType;
    }

    @NonNull
    public String getPrice() {
        return price;
    }

    @NonNull
    public String getCurrency() {
        return currency;
    }

    @NonNull
    public String getTitle() {
        return title;
    }

    @NonNull
    public String getDescription() {
        return description;
    }

    public long getMicros() {
        return micros;
    }
}
