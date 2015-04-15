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

package org.onepf.sample.trivialdrive.ui.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.onepf.opfiab.OPFIab;
import org.onepf.opfiab.api.FragmentIabHelper;
import org.onepf.opfiab.listener.OnInventoryListener;
import org.onepf.opfiab.listener.OnSetupListener;
import org.onepf.opfiab.model.event.SetupResponse;
import org.onepf.opfiab.model.event.SetupStartedEvent;
import org.onepf.opfiab.model.event.billing.InventoryResponse;
import org.onepf.sample.trivialdrive.R;
import org.onepf.sample.trivialdrive.TrivialBilling;
import org.onepf.sample.trivialdrive.ui.view.TrivialView;


public class TrivialFragment extends Fragment
        implements OnSetupListener, OnInventoryListener {

    public static TrivialFragment newInstance() {
        return new TrivialFragment();
    }


    private FragmentIabHelper iabHelper;
    private TrivialView trivialView;

    public TrivialFragment() {
        super();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.include_trivial, container, false);
    }

    @Override
    public void onViewCreated(final View view, final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        trivialView = (TrivialView) view.findViewById(R.id.trivial_drive);
    }

    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        iabHelper = OPFIab.getFragmentHelper(this);
        iabHelper.addSetupListener(this);
        iabHelper.addInventoryListener(this);

        trivialView.setIabHelper(iabHelper);

        if (savedInstanceState == null) {
            iabHelper.inventory(true);
        }
    }

    @Override
    public void onDestroyView() {
        //noinspection AssignmentToNull
        trivialView = null;
        super.onDestroyView();
    }

    @Override
    public void onSetupStarted(@NonNull final SetupStartedEvent setupStartedEvent) {
        trivialView.setEnabled(false);
    }

    @Override
    public void onSetupResponse(@NonNull final SetupResponse setupResponse) {
        trivialView.setEnabled(setupResponse.isSuccessful());
    }

    @Override
    public void onInventory(@NonNull final InventoryResponse inventoryResponse) {
        trivialView.setHasPremium(TrivialBilling.hasPremium(inventoryResponse));
        trivialView.setHasSubscription(TrivialBilling.hasValidSubscription(inventoryResponse));
    }
}
