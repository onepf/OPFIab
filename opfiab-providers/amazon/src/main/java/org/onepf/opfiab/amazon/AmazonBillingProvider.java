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

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.amazon.device.iap.model.ProductDataResponse;
import com.amazon.device.iap.model.PurchaseUpdatesResponse;
import com.amazon.device.iap.model.UserData;

import org.onepf.opfiab.BaseBillingProvider;
import org.onepf.opfiab.OPFIabUtils;
import org.onepf.opfiab.billing.BillingController;
import org.onepf.opfiab.model.billing.ConsumableDetails;
import org.onepf.opfiab.model.billing.SkuDetails;
import org.onepf.opfiab.model.billing.SkusDetails;
import org.onepf.opfiab.sku.SkuResolver;
import org.onepf.opfiab.verification.PurchaseVerifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Set;

import static org.onepf.opfiab.model.event.response.Response.Status.BILLING_UNAVAILABLE;
import static org.onepf.opfiab.model.event.response.Response.Status.SUCCESS;
import static org.onepf.opfiab.model.event.response.Response.Status.UNAUTHORISED;

public class AmazonBillingProvider extends BaseBillingProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(AmazonBillingProvider.class);


    @NonNull
    private static final String NAME = "Amazon";

    @NonNull
    private static final String PACKAGE_NAME = "com.amazon.venezia";


    private final AmazonBillingController controller = new AmazonBillingController();

    @Nullable
    private UserData userData;

    protected AmazonBillingProvider(
            @NonNull final PurchaseVerifier purchaseVerifier,
            @NonNull final SkuResolver skuResolver) {
        super(purchaseVerifier, skuResolver);
    }

    @NonNull
    @Override
    public String getName() {
        return NAME;
    }

    @Nullable
    @Override
    public String getPackageName() {
        return PACKAGE_NAME;
    }

    @NonNull
    @Override
    public BillingController getController() {
        return controller;
    }

    private boolean checkAuthorisation() {
        userData = controller.getUserData();
        if (userData == null) {
            postResponse(UNAUTHORISED);
            return false;
        }
        return true;
    }

    @Override
    public void skuDetails(@NonNull final Collection<String> skus) {
        if (!checkAuthorisation()) {
            return;
        }
        final Set<String> resolvedSkus = OPFIabUtils.resolveSkus(skuResolver, skus);
        final ProductDataResponse productDataResponse = controller.getProductData(resolvedSkus);
        final ProductDataResponse.RequestStatus requestStatus;
        switch (requestStatus = productDataResponse.getRequestStatus()) {
            case SUCCESSFUL:
                //TODO
                final SkusDetails skusDetails = new SkusDetails(null, null);
                postResponse(SUCCESS, skusDetails);
                break;
            case FAILED:
            case NOT_SUPPORTED:
                LOGGER.error("Product data request failed.", requestStatus, productDataResponse);
                postResponse(BILLING_UNAVAILABLE);
                break;
        }
    }

    @Override
    public void inventory() {
        if (!checkAuthorisation()) {
            return;
        }
        //TODO revert sku
        final PurchaseUpdatesResponse purchaseUpdatesResponse = controller.getPurchaseUpdates();
    }

    @Override
    public void purchase(@NonNull final Activity activity, @NonNull final SkuDetails skuDetails) {
        if (!checkAuthorisation()) {
            return;
        }
        final String resolvedSku = skuDetails.getSku();
    }

    @Override
    public void consume(@NonNull final ConsumableDetails consumableDetails) {
        if (!checkAuthorisation()) {
            return;
        }
        final String resolvedSku = consumableDetails.getSku();
        controller.consume(resolvedSku);
        postResponse(SUCCESS, consumableDetails);
    }


    public static class Builder extends BaseBillingProvider.Builder {

        @Override
        public BaseBillingProvider build() {
            return new AmazonBillingProvider(purchaseVerifier, skuResolver);
        }
    }
}
