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

package org.onepf.opfiab.amazon;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.amazon.device.iap.model.Product;
import com.amazon.device.iap.model.ProductDataResponse;
import com.amazon.device.iap.model.ProductType;
import com.amazon.device.iap.model.PurchaseUpdatesResponse;
import com.amazon.device.iap.model.Receipt;

import org.json.JSONException;
import org.json.JSONObject;
import org.onepf.opfiab.model.billing.Purchase;
import org.onepf.opfiab.model.billing.SkuDetails;
import org.onepf.opfiab.model.billing.SkuType;
import org.onepf.opfutils.OPFLog;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public final class AmazonUtils {

    private static final ThreadLocal<DateFormat> DATE_FORMAT = new ThreadLocal<DateFormat>() {
        @Override
        protected DateFormat initialValue() {
            return new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.getDefault());
        }
    };


    private AmazonUtils() {
        throw new UnsupportedOperationException();
    }

    @SuppressWarnings("PMD.PreserveStackTrace")
    @NonNull
    public static Date readDate(@NonNull final JSONObject jsonObject, @NonNull final String key)
            throws JSONException {

        final String date = jsonObject.getString(key);
        try {
            return DATE_FORMAT.get().parse(date);
        } catch (ParseException exception) {
            throw new JSONException("Can't parse date: " + date);
        }
    }

    /**
     * Transforms Amazon product into library SKU details model.
     *
     * @param product Amazon product to transform.
     *
     * @return Newly constructed SkuDetails object.
     */
    @Nullable
    public static SkuDetails convertSkuDetails(@NonNull final Product product) {
        final String originalJson;
        try {
            originalJson = product.toJSON().toString();
        } catch (JSONException exception) {
            OPFLog.e("Failed to set original JSON for SkuDetails.", exception);
            return null;
        }
        final SkuDetails.Builder builder = new SkuDetails.Builder(product.getSku());
        final ProductType productType = product.getProductType();
        switch (productType) {
            case CONSUMABLE:
                builder.setType(SkuType.CONSUMABLE);
                break;
            case ENTITLED:
                builder.setType(SkuType.ENTITLEMENT);
                break;
            case SUBSCRIPTION:
                builder.setType(SkuType.SUBSCRIPTION);
                break;
            default:
                OPFLog.e("Unknow Amazon product type: " + productType);
                return null;
        }
        builder.setTitle(product.getTitle());
        builder.setDescription(product.getDescription());
        builder.setPrice(product.getPrice());
        builder.setProviderName(AmazonBillingProvider.NAME);
        builder.setOriginalJson(originalJson);

        return builder.build();
    }

    /**
     * Transforms Amazon receipt into library purchase model.
     *
     * @param receipt Amazon receipt to transform.
     *
     * @return Newly constructed purchase object.
     */
    @Nullable
    public static Purchase convertPurchase(@NonNull final Receipt receipt) {
        final Purchase.Builder builder = new Purchase.Builder(receipt.getSku());
        final ProductType productType = receipt.getProductType();
        switch (productType) {
            case CONSUMABLE:
                builder.setType(SkuType.CONSUMABLE);
                break;
            case ENTITLED:
                builder.setType(SkuType.ENTITLEMENT);
                break;
            case SUBSCRIPTION:
                builder.setType(SkuType.SUBSCRIPTION);
                break;
            default:
                OPFLog.e("Unknow Amazon product type: " + productType);
                return null;
        }
        builder.setToken(receipt.getReceiptId());
        builder.setCanceled(receipt.isCanceled());
        builder.setPurchaseTime(receipt.getPurchaseDate().getTime());
        builder.setProviderName(AmazonBillingProvider.NAME);
        builder.setOriginalJson(receipt.toJSON().toString());

        return builder.build();
    }

    @NonNull
    public static Collection<SkuDetails> getSkusDetails(
            @NonNull final ProductDataResponse response) {

        final Collection<SkuDetails> skusDetails = new ArrayList<>();
        final Collection<Product> products = response.getProductData().values();
        for (final Product product : products) {
            skusDetails.add(convertSkuDetails(product));
        }
        for (final String sku : response.getUnavailableSkus()) {
            skusDetails.add(new SkuDetails(sku));
        }

        return skusDetails;
    }

    @NonNull
    public static Collection<Purchase> getInventory(
            @NonNull final PurchaseUpdatesResponse response) {

        final List<Receipt> receipts = response.getReceipts();
        final Collection<Purchase> purchases = new ArrayList<>(receipts.size());
        for (final Receipt receipt : receipts) {
            purchases.add(convertPurchase(receipt));
        }

        return purchases;
    }
}
