package org.onepf.opfiab.samsung.model;

import android.support.annotation.NonNull;

import org.json.JSONException;
import org.onepf.opfiab.model.JsonModel;

public abstract class SamsungModel extends JsonModel {

    protected static final String KEY_ITEM_ID = "mItemId";
    protected static final String KEY_ITEM_NAME = "mItemName";
    protected static final String KEY_PRICE = "mItemPrice";
    protected static final String KEY_PRICE_STRING = "mItemPriceString";
    protected static final String KEY_CURRENCY_UNIT = "mCurrencyUnit";
    protected static final String KEY_ITEM_DESC = "mItemDesc";
    protected static final String KEY_IMAGE_URL = "mItemImageUrl";
    protected static final String KEY_ITEM_DOWNLOAD_URL = "mItemDownloadUrl";


    @NonNull
    protected final String itemId;
    @NonNull
    protected final String name;
    @NonNull
    protected final Double price;
    @NonNull
    protected final String priceString;
    @NonNull
    protected final String currency;
    @NonNull
    protected final String description;
    @NonNull
    protected final String imageUrl;
    @NonNull
    protected final String downloadImageUrl;

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
