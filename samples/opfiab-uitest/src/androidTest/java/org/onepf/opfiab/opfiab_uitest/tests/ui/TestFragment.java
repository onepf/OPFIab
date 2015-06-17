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

package org.onepf.opfiab.opfiab_uitest.tests.ui;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.onepf.opfiab.OPFIab;
import org.onepf.opfiab.api.FragmentIabHelper;
import org.onepf.opfiab.api.IabHelper;
import org.onepf.opfiab.listener.OnPurchaseListener;
import org.onepf.opfiab.opfiab_uitest.R;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;

/**
 * @author antonpp
 * @since 04.06.15
 */
public class TestFragment extends Fragment {

    private static final String COLOR = "COLOR";
    private volatile FragmentIabHelper iabHelper;
    private Reference<IabHelper> helperReference;
    public TestFragment() {
    }

    public static TestFragment getInstance(int color) {
        final TestFragment fragment = new TestFragment();
        final Bundle args = new Bundle();
        args.putInt(COLOR, color);
        fragment.setArguments(args);
        return fragment;
    }

    public FragmentIabHelper getIabHelper(final OnPurchaseListener listener) {
        final IabHelper lastHelper;
        if (helperReference == null || (lastHelper = helperReference.get()) == null
                || iabHelper != lastHelper) {
            iabHelper.addPurchaseListener(listener);
            helperReference = new WeakReference<IabHelper>(iabHelper);
        }
        return iabHelper;
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.iabHelper = OPFIab.getFragmentHelper(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_empty, container, false);
    }

    @Override
    public void onViewCreated(final View view, final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.setBackgroundColor(getResources().getColor(getArguments().getInt(COLOR)));
    }

    @SuppressWarnings("AssignmentToNull")
    @Override
    public void onDestroy() {
        this.iabHelper = null;
        super.onDestroy();
    }
}
