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
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.onepf.opfiab.model.billing.SkuDetails;
import org.onepf.sample.trivialdrive.R;

public class SkuDetailsView extends LinearLayout {

    private TextView tvSkuTitle;
    private TextView tvSkuPrice;

    public SkuDetailsView(final Context context) {
        super(context);
        init();
    }

    public SkuDetailsView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SkuDetailsView(final Context context, final AttributeSet attrs,
                          final int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public SkuDetailsView(final Context context, final AttributeSet attrs,
                          final int defStyleAttr, final int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    protected void init() {
        final Context context = getContext();
        setOrientation(VERTICAL);
        setGravity(Gravity.CENTER);
        inflate(context, R.layout.view_sku_details, this);
        if (isInEditMode()) {
            return;
        }

        tvSkuTitle = (TextView) findViewById(R.id.tv_sku_title);
        tvSkuPrice = (TextView) findViewById(R.id.tv_sku_price);
    }

    public void setSkuDetails(final SkuDetails skuDetails) {
        if (skuDetails == null) {
            tvSkuPrice.setVisibility(GONE);
            tvSkuTitle.setText(R.string.sku_unknown);
        } else {
            tvSkuPrice.setVisibility(VISIBLE);
            tvSkuTitle.setText(skuDetails.getTitle());
            tvSkuPrice.setText(skuDetails.getPrice());
        }
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        return new SavedState(super.onSaveInstanceState(), this);
    }

    @Override
    protected void onRestoreInstanceState(final Parcelable state) {
        final SavedState savedState = (SavedState) state;
        super.onRestoreInstanceState(savedState.getSuperState());
        tvSkuTitle.setText(savedState.skuTitle);
        tvSkuPrice.setText(savedState.skuPrice);
        tvSkuTitle.setVisibility(savedState.titleVisibility);
        tvSkuPrice.setVisibility(savedState.priceVisibility);
    }

    protected static class SavedState extends BaseSavedState {

        private final String skuTitle;
        private final String skuPrice;
        private final int titleVisibility;
        private final int priceVisibility;

        public SavedState(final Parcelable superState,
                          final SkuDetailsView skuDetailsView) {
            super(superState);
            skuTitle = skuDetailsView.tvSkuTitle.getText().toString();
            skuPrice = skuDetailsView.tvSkuPrice.getText().toString();
            titleVisibility = skuDetailsView.tvSkuTitle.getVisibility();
            priceVisibility = skuDetailsView.tvSkuPrice.getVisibility();
        }

        public SavedState(final Parcel source) {
            super(source);
            skuTitle = source.readString();
            skuPrice = source.readString();
            titleVisibility = source.readInt();
            priceVisibility = source.readInt();
        }

        @Override
        public void writeToParcel(final Parcel dest, final int flags) {
            super.writeToParcel(dest, flags);
            dest.writeString(skuTitle);
            dest.writeString(skuPrice);
            dest.writeInt(titleVisibility);
            dest.writeInt(priceVisibility);
        }

        public static final Parcelable.Creator<SavedState> CREATOR = new Creator<SavedState>() {

            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }
}
