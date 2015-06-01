package org.onepf.opfiab.samsung.model;

import android.support.annotation.NonNull;

import org.json.JSONException;
import org.onepf.opfiab.model.JsonModel;

abstract class SamsungModel extends JsonModel {

    private static final String KEY_ITEM_ID = "mItemId";
    private static final String KEY_ITEM_NAME = "mItemName";
    private static final String KEY_PRICE = "mItemPrice";
    private static final String KEY_PRICE_STRING = "mItemPriceString";
    private static final String KEY_CURRENCY_UNIT = "mCurrencyUnit";
    private static final String KEY_ITEM_DESC = "mItemDesc";
    private static final String KEY_IMAGE_URL = "mItemImageUrl";
    private static final String KEY_ITEM_DOWNLOAD_URL = "mItemDownloadUrl";


    @NonNull
    private final String itemId;
    @NonNull
    private final String name;
    @NonNull
    private final Double price;
    @NonNull
    private final String priceString;
    @NonNull
    private final String currency;
    @NonNull
    private final String description;
    @NonNull
    private final String imageUrl;
    @NonNull
    private final String downloadImageUrl;

    public SamsungModel(@NonNull final String originalJson) throws JSONException {
        super(originalJson);
        this.itemId = jsonObject.getString(KEY_ITEM_ID);
        this.name = jsonObject.getString(KEY_ITEM_NAME);
        this.price = jsonObject.getDouble(KEY_PRICE);
        this.priceString = jsonObject.getString(KEY_PRICE_STRING);
        this.currency = jsonObject.getString(KEY_CURRENCY_UNIT);
        this.description = jsonObject.getString(KEY_ITEM_DESC);
        this.imageUrl = jsonObject.getString(KEY_IMAGE_URL);
        this.downloadImageUrl = jsonObject.getString(KEY_ITEM_DOWNLOAD_URL);
    }

    @NonNull
    public String getItemId() {
        return itemId;
    }

    @NonNull
    public String getName() {
        return name;
    }

    @NonNull
    public Double getPrice() {
        return price;
    }

    @NonNull
    public String getPriceString() {
        return priceString;
    }

    @NonNull
    public String getCurrency() {
        return currency;
    }

    @NonNull
    public String getDescription() {
        return description;
    }

    @NonNull
    public String getImageUrl() {
        return imageUrl;
    }

    @NonNull
    public String getDownloadImageUrl() {
        return downloadImageUrl;
    }
}
