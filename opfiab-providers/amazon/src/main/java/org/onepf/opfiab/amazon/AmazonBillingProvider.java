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

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.amazon.device.iap.PurchasingService;
import com.amazon.device.iap.ResponseReceiver;
import com.amazon.device.iap.model.FulfillmentResult;
import com.amazon.device.iap.model.ProductDataResponse;
import com.amazon.device.iap.model.PurchaseUpdatesResponse;

import org.onepf.opfiab.billing.BaseBillingProvider;
import org.onepf.opfiab.billing.BaseBillingProviderBuilder;
import org.onepf.opfiab.billing.BillingProvider;
import org.onepf.opfiab.billing.Compatibility;
import org.onepf.opfiab.model.billing.Purchase;
import org.onepf.opfiab.model.billing.SkuDetails;
import org.onepf.opfiab.model.event.billing.BillingRequest;
import org.onepf.opfiab.model.event.billing.ConsumeRequest;
import org.onepf.opfiab.model.event.billing.ConsumeResponse;
import org.onepf.opfiab.model.event.billing.InventoryRequest;
import org.onepf.opfiab.model.event.billing.InventoryResponse;
import org.onepf.opfiab.model.event.billing.PurchaseRequest;
import org.onepf.opfiab.model.event.billing.PurchaseResponse;
import org.onepf.opfiab.model.event.billing.SkuDetailsRequest;
import org.onepf.opfiab.model.event.billing.SkuDetailsResponse;
import org.onepf.opfiab.model.event.billing.Status;
import org.onepf.opfiab.sku.SkuResolver;
import org.onepf.opfiab.verification.PurchaseVerifier;
import org.onepf.opfutils.OPFChecks;
import org.onepf.opfutils.OPFLog;
import org.onepf.opfutils.OPFUtils;

import java.util.Collection;
import java.util.Set;
import java.util.regex.Pattern;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import static android.Manifest.permission.ACCESS_NETWORK_STATE;
import static org.onepf.opfiab.model.event.billing.Status.ITEM_ALREADY_OWNED;
import static org.onepf.opfiab.model.event.billing.Status.ITEM_UNAVAILABLE;
import static org.onepf.opfiab.model.event.billing.Status.SERVICE_UNAVAILABLE;
import static org.onepf.opfiab.model.event.billing.Status.SUCCESS;
import static org.onepf.opfiab.model.event.billing.Status.UNAUTHORISED;
import static org.onepf.opfiab.model.event.billing.Status.UNKNOWN_ERROR;

/**
 * This {@link BillingProvider} implementation adds support of
 * <a href="http://www.amazon.com/mobile-apps/b?node=2350149011">Amazon Appstore</a>
 */
@SuppressWarnings("PMD.GodClass")
public class AmazonBillingProvider extends BaseBillingProvider<SkuResolver, PurchaseVerifier> {

    public static final String NAME = "Amazon";
    protected static final String INSTALLER = "com.amazon.venezia";
    protected static final Pattern PATTERN_STORE_PACKAGE = Pattern.compile(
            "(com\\.amazon\\.venezia)|([a-z]{2,3}\\.amazon\\.mShop\\.android(\\.apk)?)");
    protected static final String TESTER_PACKAGE = "com.amazon.sdktestclient";

    /**
     * Helper object handles all Amazon SDK related calls.
     */
    protected final AmazonBillingHelper billingHelper;


    @SuppressWarnings("AssignmentToStaticFieldFromInstanceMethod")
    protected AmazonBillingProvider(
            @NonNull final Context context,
            @NonNull final SkuResolver skuResolver,
            @NonNull final PurchaseVerifier purchaseVerifier) {
        super(context, skuResolver, purchaseVerifier);
        this.billingHelper = AmazonBillingHelper.getInstance(context);
    }

    /**
     * Tries to guess appropriate error code.
     *
     * @return Most suitable status.
     */
    protected Status handleFailure() {
        // Unfortunately Amazon doesn't report a reason for error
        if (!PurchasingService.IS_SANDBOX_MODE && !OPFUtils.isConnected(context)) {
            return SERVICE_UNAVAILABLE;
        }

        return UNKNOWN_ERROR;
    }

    /**
     * Handles sku details response from Amazon.
     *
     * @param response Response to handle.
     */
    public void onEventAsync(@NonNull final ProductDataResponse response) {
        final ProductDataResponse.RequestStatus status = response.getRequestStatus();
        switch (status) {
            case SUCCESSFUL:
                final Collection<SkuDetails> skusDetails = AmazonUtils.getSkusDetails(response);
                postResponse(new SkuDetailsResponse(SUCCESS, getName(), skusDetails));
                break;
            case FAILED:
            case NOT_SUPPORTED:
                OPFLog.e("Product data request failed: %s", response);
                postResponse(new SkuDetailsResponse(handleFailure(), getName()));
                break;
            default:
                OPFLog.e("Unknown status: " + status);
                postResponse(new SkuDetailsResponse(UNKNOWN_ERROR, getName()));
                break;
        }
    }

    /**
     * Handles inventory response from Amazon.
     *
     * @param response Response to handle.
     */
    public void onEventAsync(@NonNull final PurchaseUpdatesResponse response) {
        final PurchaseUpdatesResponse.RequestStatus status = response.getRequestStatus();
        switch (status) {
            case SUCCESSFUL:
                final Collection<Purchase> inventory = AmazonUtils.getInventory(response);
                final boolean hasMore = response.hasMore();
                postResponse(new InventoryResponse(SUCCESS, getName(), inventory, hasMore));
                break;
            case FAILED:
            case NOT_SUPPORTED:
                OPFLog.e("Purchase updates request failed: %s", response);
                postResponse(new InventoryResponse(handleFailure(), getName()));
                break;
            default:
                OPFLog.e("Unknown status: " + status);
                postResponse(new InventoryResponse(UNKNOWN_ERROR, getName()));
                break;
        }
    }

    /**
     * Handles purchase response from Amazon.
     *
     * @param response Response to handle.`
     */
    public void onEventAsync(@NonNull final com.amazon.device.iap.model.PurchaseResponse response) {
        final com.amazon.device.iap.model.PurchaseResponse.RequestStatus status =
                response.getRequestStatus();
        switch (status) {
            case SUCCESSFUL:
                final Purchase purchase = AmazonUtils.convertPurchase(response.getReceipt());
                final Status responseStatus = purchase == null ? UNKNOWN_ERROR : SUCCESS;
                postResponse(new PurchaseResponse(responseStatus, getName(), purchase));
                break;
            case INVALID_SKU:
                postResponse(new PurchaseResponse(ITEM_UNAVAILABLE, getName()));
                break;
            case ALREADY_PURCHASED:
                postResponse(new PurchaseResponse(ITEM_ALREADY_OWNED, getName()));
                break;
            case FAILED:
            case NOT_SUPPORTED:
                OPFLog.e("Purchase request failed: %s", response);
                postResponse(new PurchaseResponse(handleFailure(), getName()));
                break;
            default:
                OPFLog.e("Unknown status: " + status);
                postResponse(new PurchaseResponse(UNKNOWN_ERROR, getName()));
                break;
        }
    }

    @SuppressFBWarnings({"EXS_EXCEPTION_SOFTENING_NO_CONSTRAINTS"})
    @Override
    public void checkManifest() {
        OPFChecks.checkPermission(context, ACCESS_NETWORK_STATE);
        //TODO OPFCheck.checkReceiver
        final PackageManager packageManager = context.getPackageManager();
        final ComponentName componentName = new ComponentName(context, ResponseReceiver.class);
        try {
            if (!packageManager.getReceiverInfo(componentName, 0).exported) {
                throw new IllegalStateException("Amazon receiver must be exported.");
            }
        } catch (PackageManager.NameNotFoundException exception) {
            throw new IllegalStateException(
                    "You must declare Amazon receiver to use Amazon billing provider.", exception);
        }
    }

    @Override
    public boolean isAvailable() {
        final PackageManager packageManager = context.getPackageManager();
        for (final PackageInfo info : packageManager.getInstalledPackages(0)) {
            if (PATTERN_STORE_PACKAGE.matcher(info.packageName).matches()) {
                // Check sdk tester package if app is in sandbox mode.
                return !PurchasingService.IS_SANDBOX_MODE
                        || OPFUtils.isInstalled(context, TESTER_PACKAGE);
            }
        }
        return false;
    }

    @NonNull
    @Override
    public Compatibility checkCompatibility() {
        //TODO Check Amazon classes
        if (INSTALLER.equals(OPFUtils.getPackageInstaller(context))) {
            return Compatibility.PREFERRED;
        }
        return Compatibility.COMPATIBLE;
    }

    @Override
    protected void handleRequest(@NonNull final BillingRequest billingRequest) {
        if (billingHelper.getUserData() == null) {
            postEmptyResponse(billingRequest, UNAUTHORISED);
            return;
        }
        super.handleRequest(billingRequest);
    }

    @Override
    public void skuDetails(@NonNull final SkuDetailsRequest request) {
        final Set<String> skus = request.getSkus();
        PurchasingService.getProductData(skus);
    }

    @Override
    public void inventory(@NonNull final InventoryRequest request) {
        final boolean startOver = request.startOver();
        PurchasingService.getPurchaseUpdates(startOver);
    }

    @Override
    public void purchase(@NonNull final PurchaseRequest request) {
        final String sku = request.getSku();
        PurchasingService.purchase(sku);
    }

    @Override
    public void consume(@NonNull final ConsumeRequest request) {
        final Purchase purchase = request.getPurchase();
        final String token = purchase.getToken();
        if (!TextUtils.isEmpty(token)) {
            PurchasingService.notifyFulfillment(token, FulfillmentResult.FULFILLED);
            postResponse(new ConsumeResponse(SUCCESS, getName(), purchase));
        } else {
            postEmptyResponse(request, ITEM_UNAVAILABLE);
        }
    }

    @NonNull
    @Override
    public String getName() {
        return NAME;
    }

    public static class Builder extends BaseBillingProviderBuilder<Builder, SkuResolver,
            PurchaseVerifier> {

        public Builder(@NonNull final Context context) {
            super(context);
        }

        @Override
        public AmazonBillingProvider build() {
            return new AmazonBillingProvider(context,
                    skuResolver == null ? SkuResolver.DEFAULT : skuResolver,
                    purchaseVerifier == null ? PurchaseVerifier.DEFAULT : purchaseVerifier);
        }
    }
}
