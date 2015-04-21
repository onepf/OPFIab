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

package org.onepf.trivialdrive.ui.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import org.onepf.opfiab.api.IabHelper;
import org.onepf.opfiab.model.event.billing.SkuDetailsResponse;
import org.onepf.opfiab.trivialdrive.R;
import org.onepf.trivialdrive.TrivialBilling;
import org.onepf.trivialdrive.TrivialData;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static android.view.Gravity.CENTER_HORIZONTAL;
import static android.view.Gravity.CENTER_VERTICAL;

public class TrivialView extends LinearLayout
        implements View.OnClickListener, SharedPreferences.OnSharedPreferenceChangeListener {

    public static final Set<String> SKUS = new HashSet<>(Arrays.asList(TrivialBilling.SKU_GAS,
                                                                       TrivialBilling.SKU_PREMIUM,
                                                                       TrivialBilling.SKU_SUBSCRIPTION));


    private View btnDrive;
    private View btnBuyGas;
    private View btnBuyPremium;
    private View btnBuySubscription;

    private ImageView ivCar;
    private ImageView ivGas;

    private SkuDetailsView sdvGas;
    private SkuDetailsView sdvPremium;
    private SkuDetailsView sdvSubscription;

    private IabHelper iabHelper;
    private boolean hasPremium;
    private boolean hasSubscription;

    public TrivialView(Context context) {
        super(context);
        init();
    }

    public TrivialView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TrivialView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public TrivialView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    protected void init() {
        final Context context = getContext();
        final int orientation = getResources().getConfiguration().orientation;
        setOrientation(orientation == Configuration.ORIENTATION_PORTRAIT ? VERTICAL : HORIZONTAL);
        setGravity(getOrientation() == VERTICAL ? CENTER_HORIZONTAL : CENTER_VERTICAL);
        inflate(context, R.layout.view_trivial, this);
        if (isInEditMode()) {
            return;
        }

        btnBuyGas = findViewById(R.id.btn_buy_gas);
        btnBuyPremium = findViewById(R.id.btn_buy_premium);
        btnBuySubscription = findViewById(R.id.btn_buy_subscription);
        btnDrive = findViewById(R.id.btn_drive);

        ivGas = (ImageView) findViewById(R.id.img_gas);
        ivCar = (ImageView) findViewById(R.id.img_car);

        sdvGas = (SkuDetailsView) findViewById(R.id.sdv_gas);
        sdvPremium = (SkuDetailsView) findViewById(R.id.sdv_premium);
        sdvSubscription = (SkuDetailsView) findViewById(R.id.sdv_subscription);

        btnDrive.setOnClickListener(this);
        btnBuyGas.setOnClickListener(this);
        btnBuyPremium.setOnClickListener(this);
        btnBuySubscription.setOnClickListener(this);

        setHasPremium(false);
        setHasSubscription(false);
        updateGas();
        updateButtons();

        TrivialData.registerOnSharedPreferenceChangeListener(this);
    }

    private void updateButtons() {
        btnDrive.setEnabled(canDrive());
        btnBuyGas.setEnabled(canBuyGas());
        btnBuyPremium.setEnabled(canBuyPremium());
        btnBuySubscription.setEnabled(canBuySubscription());
    }

    private boolean canDrive() {
        return hasSubscription || TrivialData.getGas() > 0;
    }

    private boolean canBuyGas() {
        return !hasSubscription && TrivialData.canAddGas();
    }

    private boolean canBuyPremium() {
        return !hasPremium;
    }

    private boolean canBuySubscription() {
        return !hasSubscription;
    }

    private void drive() {
        if (hasSubscription || TrivialData.canSpendGas()) {
            if (!hasSubscription) {
                TrivialData.spendGas();
            }
            Toast.makeText(getContext(), R.string.msg_drive_success, Toast.LENGTH_SHORT).show();
        } else {
            btnBuyGas.callOnClick();
        }
    }

    private void updateGas() {
        if (hasSubscription) {
            ivGas.setImageResource(R.drawable.img_gas_inf);
        } else {
            ivGas.setImageResource(R.drawable.img_gas_level);
            ivGas.getDrawable().setLevel(TrivialData.getGas());
        }
    }

    public void setHasPremium(final boolean hasPremium) {
        this.hasPremium = hasPremium;
        ivCar.setImageResource(hasPremium ? R.drawable.img_car_premium : R.drawable.img_car);
    }

    public void setHasSubscription(final boolean hasSubscription) {
        this.hasSubscription = hasSubscription;
        updateGas();
    }

    public void setIabHelper(final IabHelper iabHelper) {
        this.iabHelper = iabHelper;
    }

    public void setSkuDetailsResponse(final SkuDetailsResponse skuDetailsResponse) {
        sdvGas.setSkuDetails(TrivialBilling.getDetails(skuDetailsResponse, TrivialBilling.SKU_GAS));
        sdvPremium.setSkuDetails(
                TrivialBilling.getDetails(skuDetailsResponse, TrivialBilling.SKU_PREMIUM));
        sdvSubscription.setSkuDetails(
                TrivialBilling.getDetails(skuDetailsResponse, TrivialBilling.SKU_SUBSCRIPTION));
    }

    @Override
    public void onClick(final View v) {
        if (v == btnDrive) {
            drive();
        } else if (v == btnBuyGas) {
            iabHelper.purchase(TrivialBilling.SKU_GAS);
        } else if (v == btnBuyPremium) {
            iabHelper.purchase(TrivialBilling.SKU_PREMIUM);
        } else if (v == btnBuySubscription) {
            iabHelper.purchase(TrivialBilling.SKU_SUBSCRIPTION);
        }
        updateButtons();
    }

    @Override
    protected void onDetachedFromWindow() {
        TrivialData.unregisterOnSharedPreferenceChangeListener(this);
        super.onDetachedFromWindow();
    }

    @Override
    public void onSharedPreferenceChanged(final SharedPreferences sharedPreferences,
                                          final String key) {
        updateGas();
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        return new SavedState(super.onSaveInstanceState(), this);
    }

    @Override
    protected void onRestoreInstanceState(final Parcelable state) {
        final SavedState savedState = (SavedState) state;
        super.onRestoreInstanceState(savedState.getSuperState());
        setHasPremium(savedState.hasPremium);
        setHasSubscription(savedState.hasSubscription);
    }


    protected static class SavedState extends BaseSavedState {

        public static final Parcelable.Creator<SavedState> CREATOR = new Creator<SavedState>() {

            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
        private final boolean hasPremium;
        private final boolean hasSubscription;

        public SavedState(final Parcelable superState,
                          final TrivialView trivialView) {
            super(superState);
            hasPremium = trivialView.hasPremium;
            hasSubscription = trivialView.hasSubscription;
        }

        public SavedState(final Parcel source) {
            super(source);
            hasPremium = source.readByte() != 0;
            hasSubscription = source.readByte() != 0;
        }

        @Override
        public void writeToParcel(final Parcel dest, final int flags) {
            super.writeToParcel(dest, flags);
            dest.writeByte((byte) (hasPremium ? 1 : 0));
            dest.writeByte((byte) (hasSubscription ? 1 : 0));
        }
    }
}
