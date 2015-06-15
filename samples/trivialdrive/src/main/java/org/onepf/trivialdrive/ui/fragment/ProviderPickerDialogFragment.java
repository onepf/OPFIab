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

package org.onepf.trivialdrive.ui.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.onepf.trivialdrive.OnProviderPickerListener;
import org.onepf.trivialdrive.Provider;
import org.onepf.trivialdrive.R;
import org.onepf.trivialdrive.TrivialApplication;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ProviderPickerDialogFragment extends DialogFragment {

    private static final String ARG_PROVIDERS = "providers";

    public static ProviderPickerDialogFragment getInstance(
            final Collection<Provider> providers) {
        final ProviderPickerDialogFragment fragment = new ProviderPickerDialogFragment();
        final Bundle bundle = new Bundle();
        bundle.putSerializable(ARG_PROVIDERS, new ArrayList<>(providers));
        fragment.setArguments(bundle);
        return fragment;
    }


    private List<Provider> providers;
    private OnProviderPickerListener listener;


    public void setListener(final OnProviderPickerListener listener) {
        this.listener = listener;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        providers = (List<Provider>) getArguments().getSerializable(ARG_PROVIDERS);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        if (listener == null) {
            throw new IllegalStateException();
        }
        final OnProviderPickerListener listener = this.listener;
        final Adapter adapter = new Adapter();
        final Dialog dialog =  new AlertDialog.Builder(getActivity())
                .setCancelable(true)
                .setTitle(R.string.dialog_providers_title)
                .setAdapter(adapter, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog, final int which) {
                        final Provider provider = adapter.getItem(which);
                        listener.onProviderPicked(provider);
                        dialog.dismiss();
                    }
                })
                .create();
        dialog.setCanceledOnTouchOutside(true);
        return dialog;
    }

    @Override
    public void onDetach() {
        listener = null;
        super.onDetach();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        TrivialApplication.getRefWatcher(getActivity()).watch(this);
    }

    private class Adapter extends ArrayAdapter<Provider> {

        private final LayoutInflater inflater;

        public Adapter() {
            super(getActivity(), 0, providers);
            inflater = getActivity().getLayoutInflater();
        }

        @Override
        public View getView(final int position, final View convertView, final ViewGroup parent) {
            final View view;
            if (convertView != null) {
                view = convertView;
            } else {
                view = inflater.inflate(android.R.layout.simple_list_item_1, parent, false);
            }

            final TextView textView = (TextView) view.findViewById(android.R.id.text1);
            textView.setText(getItem(position).getNameId());

            return view;
        }
    }
}
