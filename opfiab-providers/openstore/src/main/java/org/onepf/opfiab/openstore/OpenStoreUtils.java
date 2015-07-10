package org.onepf.opfiab.openstore;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.json.JSONException;
import org.onepf.opfiab.model.billing.Purchase;
import org.onepf.opfiab.model.billing.SignedPurchase;
import org.onepf.opfiab.model.billing.SkuDetails;
import org.onepf.opfiab.model.billing.SkuType;
import org.onepf.opfiab.openstore.model.OpenPurchase;
import org.onepf.opfiab.openstore.model.OpenSkuDetails;
import org.onepf.opfiab.openstore.model.PurchaseState;
import org.onepf.opfiab.util.OPFIabUtils;
import org.onepf.opfutils.OPFLog;
import org.onepf.opfutils.OPFUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class OpenStoreUtils {

    public static final String ACTION_BIND_OPENSTORE = "org.onepf.oms.openappstore.BIND";

    private static final String RESPONSE_CODE = "RESPONSE_CODE";
    private static final String SKU_DETAILS_LIST = "DETAILS_LIST";
    private static final String SKU_LIST = "ITEM_ID_LIST";
    private static final String BUY_INTENT = "BUY_INTENT";
    private static final String PURCHASE_DATA = "INAPP_PURCHASE_DATA";
    private static final String SIGNATURE = "INAPP_DATA_SIGNATURE";
    //    private static final String ITEM_LIST = "INAPP_PURCHASE_ITEM_LIST";
    private static final String PURCHASE_DATA_LIST = "INAPP_PURCHASE_DATA_LIST";
    private static final String SIGNATURE_LIST = "INAPP_DATA_SIGNATURE_LIST";
    private static final String CONTINUATION_TOKEN = "INAPP_CONTINUATION_TOKEN";


    @Nullable
    public static Intent getOpenStoreIntent(@NonNull final Context context) {
        final Intent intent = new Intent(ACTION_BIND_OPENSTORE);
        final PackageManager packageManager = context.getPackageManager();
        final List<ResolveInfo> resolveInfos = packageManager.queryIntentServices(intent, 0);
        if (resolveInfos != null && !resolveInfos.isEmpty()) {
            final Intent explicitIntent = new Intent(intent);
            final ServiceInfo serviceInfo = resolveInfos.get(0).serviceInfo;
            explicitIntent.setClassName(serviceInfo.packageName, serviceInfo.name);
            return explicitIntent;
        }
        return null;
    }

    @NonNull
    public static OpenStoreIntentMaker getIntentMaker(@NonNull final String name,
                                                      @NonNull final String... packages) {
        //noinspection OverlyComplexAnonymousInnerClass
        return new OpenStoreIntentMaker() {
            @Nullable
            @Override
            public Intent makeIntent(@NonNull final Context context) {
                for (final String packageName : packages) {
                    if (OPFUtils.isInstalled(context, packageName)) {
                        final Intent intent = new Intent(ACTION_BIND_OPENSTORE);
                        intent.setPackage(packageName);
                        return intent;
                    }
                }
                return null;
            }

            @NonNull
            @Override
            public String getProviderName() {
                return name;
            }
        };
    }

    @NonNull
    public static Bundle putSkus(@NonNull final Bundle bundle,
                                 @NonNull final Collection<String> skus) {
        return OPFIabUtils.putList(bundle, new ArrayList<>(skus), SKU_LIST);
    }

    @NonNull
    public static Bundle addSkuDetails(@NonNull final Bundle bundle,
                                       @Nullable final Bundle source) {
        final ArrayList<String> skuDetailsList = getSkusDetailsList(source);
        return OPFIabUtils.addList(bundle, skuDetailsList, SKU_DETAILS_LIST);
    }

    @Nullable
    public static String getPurchaseData(@Nullable final Bundle bundle) {
        if (bundle == null) {
            return null;
        }
        return bundle.getString(PURCHASE_DATA);
    }

    @Nullable
    public static String getSignature(@Nullable final Bundle bundle) {
        if (bundle == null) {
            return null;
        }
        return bundle.getString(SIGNATURE);
    }

    @Nullable
    public static String getContinuationToken(@Nullable final Bundle bundle) {
        if (bundle == null) {
            return null;
        }
        return bundle.getString(CONTINUATION_TOKEN);
    }

    @Nullable
    public static Response getResponse(@Nullable final Bundle bundle) {
        if (bundle == null || !bundle.containsKey(RESPONSE_CODE)) {
            return null;
        }
        final int responseCode = bundle.getInt(RESPONSE_CODE);
        return Response.fromCode(responseCode);
    }

    @Nullable
    public static PendingIntent getPurchaseIntent(@Nullable final Bundle bundle) {
        if (bundle == null) {
            return null;
        }
        return bundle.getParcelable(BUY_INTENT);
    }

    @Nullable
    public static OpenPurchase getPurchase(@Nullable final Bundle bundle) {
        final String purchaseData = getPurchaseData(bundle);
        if (purchaseData == null) {
            return null;
        }
        try {
            return new OpenPurchase(purchaseData);
        } catch (JSONException exception) {
            OPFLog.e("", exception);
        }
        return null;
    }

    @SuppressWarnings("PMD.LooseCoupling")
    @Nullable
    public static ArrayList<String> getSkusDetailsList(@Nullable final Bundle bundle) {
        return OPFIabUtils.getList(bundle, SKU_DETAILS_LIST);
    }

    @SuppressWarnings({"PMD.LooseCoupling", "CollectionDeclaredAsConcreteClass"})
    @Nullable
    public static ArrayList<String> getPurchasesList(@Nullable final Bundle bundle) {
        return OPFIabUtils.getList(bundle, PURCHASE_DATA_LIST);
    }

    @SuppressWarnings({"PMD.LooseCoupling", "CollectionDeclaredAsConcreteClass"})
    @Nullable
    public static ArrayList<String> getSignaturesList(@Nullable final Bundle bundle) {
        return OPFIabUtils.getList(bundle, SIGNATURE_LIST);
    }

    @Nullable
    public static Collection<OpenSkuDetails> getSkusDetails(@Nullable final Bundle bundle) {
        final Collection<String> skusDetailsList = getSkusDetailsList(bundle);
        if (skusDetailsList == null) {
            return null;
        }
        final Collection<OpenSkuDetails> skusDetails = new ArrayList<>(skusDetailsList.size());
        for (final String skuDetails : skusDetailsList) {
            try {
                skusDetails.add(new OpenSkuDetails(skuDetails));
            } catch (JSONException exception) {
                OPFLog.e("", exception);
            }
        }
        return skusDetails;
    }

    @Nullable
    public static Collection<OpenPurchase> getPurchases(@Nullable final Bundle bundle) {
        final Collection<String> purchasesList = getPurchasesList(bundle);
        if (purchasesList == null) {
            return null;
        }
        final Collection<OpenPurchase> purchases = new ArrayList<>(purchasesList.size());
        for (final String purchase : purchasesList) {
            try {
                purchases.add(new OpenPurchase(purchase));
            } catch (JSONException exception) {
                OPFLog.e("", exception);
            }
        }
        return purchases;
    }


    @NonNull
    public static SkuDetails convertSkuDetails(@NonNull final OpenSkuDetails openSkuDetails,
                                               @NonNull final String name,
                                               @NonNull final SkuType skuType) {
        return new SkuDetails.Builder(openSkuDetails.getProductId())
                .setType(skuType)
                .setProviderName(name)
                .setTitle(openSkuDetails.getTitle())
                .setPrice(openSkuDetails.getPrice())
                .setDescription(openSkuDetails.getDescription())
                .setOriginalJson(openSkuDetails.getOriginalJson())
                .build();
    }

    @NonNull
    public static Purchase convertPurchase(@NonNull final OpenPurchase openPurchase,
                                           @NonNull final String name,
                                           @NonNull final SkuType skuType,
                                           @Nullable final String signature) {
        return new SignedPurchase.Builder(openPurchase.getProductId())
                .setType(skuType)
                .setProviderName(name)
                .setSignature(signature)
                .setToken(openPurchase.getPurchaseToken())
                .setPurchaseTime(openPurchase.getPurchaseTime())
                .setOriginalJson(openPurchase.getOriginalJson())
                .setCanceled(openPurchase.getPurchaseState() != PurchaseState.PURCHASED)
                .build();

    }

    private OpenStoreUtils() {
        throw new UnsupportedOperationException();
    }
}
