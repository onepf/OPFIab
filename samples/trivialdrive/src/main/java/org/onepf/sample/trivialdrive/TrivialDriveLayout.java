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

package org.onepf.sample.trivialdrive;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

/**
 * @author antonpp
 * @since 06.04.15
 */
public class TrivialDriveLayout extends RelativeLayout {

    // A value that indicates that user has infinite gas
    private static final int INFINITE_GAS_LEVEL = 5;

    private ImageView buyGasButton;
    private ImageView driveButton;
    private ImageView gasGaugeImageView;
    private ImageView upgradeButton;
    private ImageView infiniteGasButton;
    private ImageView freeOrPremiumImageView;
    private ImageView waitScreenImageView;
    private LinearLayout mainScreenImageView;

    public TrivialDriveLayout(Context context) {
        super(context);
        init();
    }

    public TrivialDriveLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TrivialDriveLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public TrivialDriveLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    protected void init() {
        inflate(getContext(), R.layout.trivial_drive_layout, this);

        driveButton = (ImageView) findViewById(R.id.drive_button);
        buyGasButton = (ImageView) findViewById(R.id.buy_gas_button);
        gasGaugeImageView = (ImageView) findViewById(R.id.gas_gauge);
        upgradeButton = (ImageView) findViewById(R.id.upgrade_button);
        infiniteGasButton = (ImageView) findViewById(R.id.infinite_gas_button);
        freeOrPremiumImageView = (ImageView) findViewById(R.id.free_or_premium);
        waitScreenImageView = (ImageView) findViewById(R.id.screen_wait);
        mainScreenImageView = (LinearLayout) findViewById(R.id.screen_main);
    }

    public void setBuyGasClickListener(OnClickListener listener) {
        buyGasButton.setOnClickListener(listener);
    }

    public void setDriveCliclListener(OnClickListener listener) {
        driveButton.setOnClickListener(listener);
    }

    public void setInfiniteGasClickListener(OnClickListener listener) {
        infiniteGasButton.setOnClickListener(listener);
    }

    public void setUpgradeCarClickListener(OnClickListener listener) {
        upgradeButton.setOnClickListener(listener);
    }

    public void setIsPremium(boolean isPremium) {
        freeOrPremiumImageView.setImageResource(isPremium ? R.drawable.premium : R.drawable.free);
    }

    public void setIsSubscribedToInfiniteGas(boolean isSubscribedToInfiniteGas) {
        if (isSubscribedToInfiniteGas) {
            gasGaugeImageView.setImageLevel(INFINITE_GAS_LEVEL);
        }
    }

    public void setGasLevel(int gasLevel) {
        gasGaugeImageView.setImageLevel(gasLevel);
    }

    // Enables or disables the "please wait" screen.
    private void setWaitScreen(boolean set) {
        mainScreenImageView.setVisibility(set ? View.GONE : View.VISIBLE);
        waitScreenImageView.setVisibility(set ? View.VISIBLE : View.GONE);
    }

    public void setEnabledBillingButtons(boolean isEnabled) {
        buyGasButton.setEnabled(isEnabled);
        upgradeButton.setEnabled(isEnabled);
        infiniteGasButton.setEnabled(isEnabled);
    }
}
