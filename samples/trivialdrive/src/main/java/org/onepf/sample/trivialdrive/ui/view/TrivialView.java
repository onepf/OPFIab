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

package org.onepf.sample.trivialdrive.ui.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import org.onepf.sample.trivialdrive.R;
import org.onepf.sample.trivialdrive.TrivialUtils;

import static org.onepf.sample.trivialdrive.TrivialUtils.KEY_GAS;
import static org.onepf.sample.trivialdrive.TrivialUtils.KEY_PREMIUM;

public class TrivialView extends RelativeLayout
        implements SharedPreferences.OnSharedPreferenceChangeListener {

    private SharedPreferences preferences;

    private View btnBuyGas;
    private View btnBuyPremium;
    private View btnBuySubscription;

    private ImageView imgCar;
    private ImageView imgGas;

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

    private void init() {
        final Context context = getContext();
        inflate(context, R.layout.view_trivial, this);
        if (isInEditMode()) {
            return;
        }

        preferences = TrivialUtils.getPreferences(context);

        btnBuyGas = findViewById(R.id.btn_buy_gas);
        btnBuyPremium = findViewById(R.id.btn_buy_premium);
        btnBuySubscription = findViewById(R.id.btn_buy_subscription);

        imgGas = (ImageView) findViewById(R.id.img_gas);
        imgCar = (ImageView) findViewById(R.id.img_car);

        findViewById(R.id.btn_drive).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View v) {
                drive();
            }
        });

        setButtonsEnabled(false);
        setHasSubscription(false);
        updateGas();
        updatePremium();

        preferences.registerOnSharedPreferenceChangeListener(this);
    }

    private void drive() {
        final int gas = preferences.getInt(KEY_GAS, 0);
        final int msgId;
        if (!hasSubscription && gas <= 0) {
            msgId = R.string.msg_drive_failed;
        } else {
            preferences.edit().putInt(KEY_GAS, gas - 1).apply();
            updateGas();
            msgId = R.string.msg_drive_success;
        }
        Toast.makeText(getContext(), msgId, Toast.LENGTH_SHORT).show();
    }

    private void updateGas() {
        final int level = preferences.getInt(KEY_GAS, 0);
        imgGas.getDrawable().setLevel(level);
    }

    private void updatePremium() {
        final boolean hasPremium = preferences.getBoolean(KEY_PREMIUM, false);
        imgCar.setImageResource(hasPremium ? R.drawable.img_car_premium : R.drawable.img_car);
    }

    public void setBuyGasClickListener(final OnClickListener listener) {
        btnBuyGas.setOnClickListener(listener);
    }

    public void setBuySubscriptionListener(final OnClickListener listener) {
        btnBuySubscription.setOnClickListener(listener);
    }

    public void setBuyPremiumClickListener(final OnClickListener listener) {
        btnBuyPremium.setOnClickListener(listener);
    }

    public void setButtonsEnabled(final boolean enabled) {
        btnBuyGas.setEnabled(enabled);
        btnBuyPremium.setEnabled(enabled);
        btnBuySubscription.setEnabled(enabled);
    }

    public void setHasSubscription(final boolean hasSubscription) {
        this.hasSubscription = hasSubscription;
        if (hasSubscription) {
            imgGas.setImageResource(R.drawable.img_gas_inf);
        } else {
            imgGas.setImageResource(R.drawable.img_gas_level);
            updateGas();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        preferences.unregisterOnSharedPreferenceChangeListener(this);
        super.onDetachedFromWindow();
    }

    @Override
    public void onSharedPreferenceChanged(final SharedPreferences sharedPreferences,
                                          final String key) {
        switch (key) {
            case KEY_GAS:
                updateGas();
                break;
            case KEY_PREMIUM:
                updatePremium();
                break;
            default:
        }
    }
}
