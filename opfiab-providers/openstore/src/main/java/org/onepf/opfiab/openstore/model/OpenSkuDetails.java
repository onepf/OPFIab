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

package org.onepf.opfiab.openstore.model;

import android.support.annotation.NonNull;

import org.json.JSONException;

public class OpenSkuDetails extends OpenBillingModel {

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

    public OpenSkuDetails(@NonNull final String originalJson) throws JSONException {
        super(originalJson);
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

    /**
     * Gets type of product, can be an in-app or a subscription.
     *
     * @return Product type, can't be null.
     */
    @NonNull
    public ItemType getItemType() {
        return itemType;
    }

    /**
     * Gets formatted price of the item, including its currency sign. The price does not include
     * tax.
     *
     * @return Product price, can't be null.
     */
    @NonNull
    public String getPrice() {
        return price;
    }

    /**
     * Gets <a href="http://en.wikipedia.org/wiki/ISO_4217#Active_codes">ISO 4217</a> currency code
     * for price. For example, if price is specified in British pounds sterling, then
     * price_currency_code is "GBP".
     *
     * @return Currency code, can't be null.
     */
    @NonNull
    public String getCurrency() {
        return currency;
    }

    /**
     * Gets the title of the product.
     *
     * @return Product title, can't be null.
     */
    @NonNull
    public String getTitle() {
        return title;
    }

    /**
     * Gets the description of the product.
     *
     * @return Product description, can't be null.
     */
    @NonNull
    public String getDescription() {
        return description;
    }

    /**
     * Gets price in micro-units, where 1,000,000 micro-units equal one unit of the currency. For
     * example, if price is "€7.99", price_amount_micros is "7990000".
     *
     * @return Price amount in micros.
     */
    public long getMicros() {
        return micros;
    }
}
