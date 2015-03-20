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

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.onepf.opfiab.FragmentIabHelper;
import org.onepf.opfiab.OPFIab;
import org.onepf.opfiab.listener.OnSetupListener;
import org.onepf.opfiab.listener.OnSkuDetailsListener;
import org.onepf.opfiab.model.event.SetupResponse;
import org.onepf.opfiab.model.event.billing.SkuDetailsResponse;

import static org.onepf.sample.trivialdrive.TrivialConstants.SKU_GAS;


public class TrivialFragment extends Fragment
        implements View.OnClickListener, OnSetupListener, OnSkuDetailsListener {

    public static TrivialFragment newInstance() {
        return new TrivialFragment();
    }


    private FragmentIabHelper iabHelper;
    private View btnBuy;

    public TrivialFragment() {
        super();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.trivial_layout, container, false);
    }

    @Override
    public void onViewCreated(final View view, final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        btnBuy = view.findViewById(R.id.btn_buy);
        btnBuy.setEnabled(false);
        btnBuy.setOnClickListener(this);
    }

    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        iabHelper = OPFIab.getFragmentHelper(this);
        iabHelper.addSetupListener(this);
        iabHelper.addSkuDetailsListener(this);
        if (savedInstanceState == null) {
            iabHelper.skuDetails(SKU_GAS);
        }
    }

    @Override
    public void onDestroyView() {
        //noinspection AssignmentToNull
        btnBuy = null;
        super.onDestroyView();
    }

    @Override
    public void onClick(final View v) {
        iabHelper.purchase(SKU_GAS);
    }

    @Override
    public void onSetup(@NonNull final SetupResponse setupResponse) {
        btnBuy.setEnabled(setupResponse.isSuccessful());
    }

    @Override
    public void onSkuDetails(@NonNull final SkuDetailsResponse skuDetailsResponse) {
        if (skuDetailsResponse.isSuccessful()) {
            iabHelper.inventory(true);
        }
    }
}
