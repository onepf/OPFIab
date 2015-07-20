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

package org.onepf.opfiab.amazon.model;

import android.support.annotation.NonNull;

import com.amazon.device.iap.internal.model.ProductBuilder;
import com.amazon.device.iap.model.Product;
import com.amazon.device.iap.model.ProductType;

import org.json.JSONException;

public class AmazonSkuDetails extends AmazonModel {

    protected static final String NAME_ITEM_TYPE = "productType";
    protected static final String NAME_TITLE = "title";
    protected static final String NAME_DESCRIPTION = "description";
    protected static final String NAME_PRICE = "price";
    protected static final String NAME_SMALL_ICON_URL = "smallIconUrl";

    @NonNull
    protected final Product product;

    public AmazonSkuDetails(@NonNull final String originalJson) throws JSONException {
        super(originalJson);
        final ProductType productType = ProductType.valueOf(jsonObject.getString(NAME_ITEM_TYPE));
        if (productType == null) {
            throw new JSONException("Unknown product type.");
        }
        final ProductBuilder builder = new ProductBuilder()
                .setSku(sku)
                .setTitle(jsonObject.getString(NAME_TITLE))
                .setDescription(jsonObject.getString(NAME_DESCRIPTION))
                .setPrice(jsonObject.getString(NAME_PRICE))
                .setSmallIconUrl(jsonObject.getString(NAME_SMALL_ICON_URL));
        builder.setProductType(productType);
        this.product = new Product(builder);
    }

    /**
     * @see Product#getSku()
     */
    @NonNull
    public String getSku() {

        return product.getSku();
    }
    /**
     * @see Product#getTitle()
     */
    @NonNull
    public String getTitle() {
        return product.getTitle();
    }

    /**
     * @see Product#getDescription()
     */
    @NonNull
    public String getDescription() {
        return product.getDescription();
    }

    /**
     * @see Product#getProductType()
     */
    @NonNull
    public ProductType getProductType() {
        return product.getProductType();
    }

    /**
     * @see Product#getPrice()
     */
    @NonNull
    public String getPrice() {
        return product.getPrice();
    }

    /**
     * @see Product#getSmallIconUrl()
     */
    @NonNull
    public String getSmallIconUrl() {
        return product.getSmallIconUrl();
    }
}
