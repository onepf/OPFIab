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

import com.amazon.device.iap.PurchasingListener;
import com.amazon.device.iap.PurchasingService;
import com.amazon.device.iap.model.ProductDataResponse;
import com.amazon.device.iap.model.PurchaseResponse;
import com.amazon.device.iap.model.PurchaseUpdatesResponse;
import com.amazon.device.iap.model.UserDataResponse;

import org.onepf.opfiab.OPFIab;
import org.onepf.opfiab.billing.BillingController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;

public class AmazonBillingController implements BillingController, PurchasingListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(AmazonBillingController.class);


    private volatile CountDownLatch userDataLatch;

    public AmazonBillingController() {
        PurchasingService.registerListener(OPFIab.getContext(), this);
    }

    @Override
    public boolean isBillingSupported() {
        return true;
    }

    @Override
    public void onUserDataResponse(final UserDataResponse userDataResponse) {

    }

    @Override
    public void onProductDataResponse(final ProductDataResponse productDataResponse) {

    }

    @Override
    public void onPurchaseResponse(final PurchaseResponse purchaseResponse) {

    }

    @Override
    public void onPurchaseUpdatesResponse(final PurchaseUpdatesResponse purchaseUpdatesResponse) {

    }



}
