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

    private static final String NAME_PRICE = "price";
    private static final String NAME_CURRENCY = "price_currency_code";
    private static final String NAME_TITLE = "title";
    private static final String NAME_DESCRIPTION = "description";
    private static final String NAME_MICROS = "price_amount_micros";


    @NonNull
    private final String price;
    @NonNull
    private final String currency;
    @NonNull
    private final String title;
    @NonNull
    private final String description;
    private final long micros;

    public GoogleSkuDetails(@NonNull final JSONObject json) throws JSONException {
        super(json);
        this.price = json.getString(NAME_PRICE);
        this.micros = json.getLong(NAME_MICROS);
        this.currency = json.getString(NAME_CURRENCY);
        this.title = json.getString(NAME_TITLE);
        this.description = json.getString(NAME_DESCRIPTION);
    }

    public GoogleSkuDetails(@NonNull final String json) throws JSONException {
        this(new JSONObject(json));
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
