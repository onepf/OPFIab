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

    protected static final String NAME_TYPE = "type";
    protected static final String NAME_PRICE = "price";
    protected static final String NAME_TITLE = "title";
    protected static final String NAME_DESCRIPTION = "description";

    @NonNull
    protected final ItemType itemType;
    @NonNull
    protected final String price;
    @NonNull
    protected final String title;
    @NonNull
    protected final String description;

    public OpenSkuDetails(@NonNull final String originalJson) throws JSONException {
        super(originalJson);
        final String itemTypeCode = jsonObject.getString(NAME_TYPE);
        final ItemType itemType = ItemType.fromCode(itemTypeCode);
        if (itemType == null) {
            throw new JSONException("Unrecognized itemType: " + itemTypeCode);
        }
        this.itemType = itemType;

        this.price = jsonObject.getString(NAME_PRICE);
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
}
