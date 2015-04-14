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

package org.onepf.sample.trivialdrive.ui.activity;

import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.makeramen.dragsortadapter.DragSortAdapter;
import com.makeramen.dragsortadapter.DragSortShadowBuilder;

import org.onepf.opfiab.OPFIab;
import org.onepf.opfiab.api.AdvancedIabHelper;
import org.onepf.opfiab.listener.OnSetupListener;
import org.onepf.opfiab.model.BillingProviderInfo;
import org.onepf.opfiab.model.event.SetupResponse;
import org.onepf.opfiab.model.event.SetupStartedEvent;
import org.onepf.sample.trivialdrive.Helper;
import org.onepf.sample.trivialdrive.OnProviderPickerListener;
import org.onepf.sample.trivialdrive.Provider;
import org.onepf.sample.trivialdrive.R;
import org.onepf.sample.trivialdrive.TrivialBilling;
import org.onepf.sample.trivialdrive.ui.fragment.ProviderPickerDialogFragment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

abstract class TrivialActivity extends ActionBarActivity
        implements OnProviderPickerListener {

    private static final String FRAGMENT_PROVIDER_PICKER = "provider_picker";


    private AdvancedIabHelper iabHelper;

    private DrawerLayout drawerLayout;
    private RecyclerView recyclerView;
    private Adapter adapter;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        iabHelper = OPFIab.getAdvancedHelper();
        iabHelper.register();
        final FragmentManager fragmentManager = getSupportFragmentManager();
        final ProviderPickerDialogFragment fragment = (ProviderPickerDialogFragment) fragmentManager
                .findFragmentByTag(FRAGMENT_PROVIDER_PICKER);
        if (fragment != null) {
            fragment.setListener(this);
        }
    }

    @Override
    public void setContentView(final int layoutResID) {
        super.setContentView(R.layout.activity_trivial);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer);
        final View view = getLayoutInflater().inflate(layoutResID, drawerLayout, false);
        drawerLayout.addView(view, 0);

        recyclerView = (RecyclerView) findViewById(R.id.recycler);
        adapter = new Adapter(recyclerView);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    protected void onDestroy() {
        iabHelper.unregister();
        super.onDestroy();
    }

    @Override
    public void onProviderPicked(final Provider provider) {
        adapter.addItem(provider);
    }

    private class Adapter extends DragSortAdapter<DragSortAdapter.ViewHolder> {

        private static final int TYPE_HEADER = 0;
        private static final int TYPE_FOOTER = 1;
        private static final int TYPE_ITEM = 2;

        private final List<Provider> items;
        private final LayoutInflater inflater;

        public Adapter(final RecyclerView recyclerView) {
            super(recyclerView);
            inflater = getLayoutInflater();
            items = new ArrayList<>(TrivialBilling.getProviders());
        }

        private boolean showFooter() {
            return items.size() < Provider.values().length;
        }

        private void update() {
            TrivialBilling.setProviders(items);
            notifyDataSetChanged();
        }

        private void addItem(final Provider provider) {
            if (items.contains(provider)) {
                return;
            }
            items.add(provider);
            update();
        }

        private void deleteItem(final Provider provider) {
            final int index;
            if ((index = items.indexOf(provider)) < 0) {
                return;
            }
            items.remove(index);
            update();
        }

        @Override
        public ViewHolder onCreateViewHolder(final ViewGroup parent,
                                             final int viewType) {
            final View view;
            switch (viewType) {
                case TYPE_HEADER:
                    view = inflater.inflate(R.layout.item_drawer_header, parent, false);
                    return new HeaderViewHolder(this, view);
                case TYPE_FOOTER:
                    view = inflater.inflate(R.layout.item_drawer_footer, parent, false);
                    return new FooterViewHolder(this, view);
                case TYPE_ITEM:
                    view = inflater.inflate(R.layout.item_drawer, parent, false);
                    return new ItemViewHolder(this, view);
                default:
                    throw new IllegalStateException();
            }
        }

        @Override
        public void onBindViewHolder(final DragSortAdapter.ViewHolder holder, final int position) {
            if (getItemViewType(position) == TYPE_ITEM) {
                final ItemViewHolder viewHolder = (ItemViewHolder) holder;
                final Provider provider = items.get(position - 1);
                viewHolder.setProvider(provider);
            }
        }

        @Override
        public int getItemCount() {
            return items.size() + 1 + (showFooter() ? 1 : 0);
        }

        @Override
        public long getItemId(final int position) {
            return position;
        }

        @Override
        public int getItemViewType(final int position) {
            if (position == 0) {
                return TYPE_HEADER;
            }

            if (position == items.size() + 1) {
                return TYPE_FOOTER;
            }

            return TYPE_ITEM;
        }

        @Override
        public int getPositionForId(final long l) {
            return (int) l;
        }

        @Override
        public boolean move(final int from, final int to) {
            if (getItemViewType(to) != TYPE_ITEM) {
                return false;
            }
            items.add(from, items.remove(to));
            return true;
        }
    }

    private class HeaderViewHolder extends DragSortAdapter.ViewHolder
            implements View.OnClickListener, OnSetupListener, AdapterView.OnItemSelectedListener {

        private final Spinner spinHelper;
        private final TextView tvSetup;
        private final ProgressBar pgSetup;
        private final Button btnInit;
        private final Button btnSetup;
        private final CheckedTextView ctvSkipUnauthorized;
        private final CheckedTextView ctvAutoRecover;

        public HeaderViewHolder(final DragSortAdapter<?> dragSortAdapter, final View itemView) {
            super(dragSortAdapter, itemView);
            spinHelper = (Spinner) itemView.findViewById(R.id.spin_helper);
            tvSetup = (TextView) itemView.findViewById(R.id.tv_setup);
            pgSetup = (ProgressBar) itemView.findViewById(R.id.pg_setup);
            btnInit = (Button) itemView.findViewById(R.id.btn_init);
            btnSetup = (Button) itemView.findViewById(R.id.btn_setup);
            ctvAutoRecover = (CheckedTextView) itemView.findViewById(R.id.ctv_auto_recover);
            ctvSkipUnauthorized = (CheckedTextView) itemView
                    .findViewById(R.id.ctv_skip_unauthorized);

            final HelpersAdapter adapter = new HelpersAdapter();
            spinHelper.setAdapter(adapter);
            spinHelper.setSelection(adapter.getPosition(TrivialBilling.getHelper()));
            spinHelper.setOnItemSelectedListener(this);

            btnInit.setOnClickListener(this);
            btnSetup.setOnClickListener(this);
            ctvAutoRecover.setChecked(TrivialBilling.isAutoRecover());
            ctvAutoRecover.setOnClickListener(this);
            ctvSkipUnauthorized.setOnClickListener(this);
            ctvSkipUnauthorized.setChecked(TrivialBilling.isSkipUnauthorized());

            iabHelper.addSetupListener(this);
        }

        @Override
        public void onItemSelected(final AdapterView<?> parent, final View view, final int position,
                                   final long id) {
            final Helper helper = (Helper) parent.getItemAtPosition(position);
            if (helper != TrivialBilling.getHelper()) {
                TrivialBilling.setHelper(helper);
                startActivity(new Intent(TrivialActivity.this, LauncherActivity.class));
                finish();
            }
        }

        @Override
        public void onNothingSelected(final AdapterView<?> parent) { }

        @Override
        public void onClick(final View v) {
            if (v == btnInit) {
                OPFIab.init(getApplication(), TrivialBilling.getRelevantConfiguration());
            } else if (v == btnSetup) {
                OPFIab.setup();
            } else if (v == ctvAutoRecover) {
                ctvAutoRecover.toggle();
                TrivialBilling.setAutoRecover(ctvAutoRecover.isChecked());
            } else if (v == ctvSkipUnauthorized) {
                ctvSkipUnauthorized.toggle();
                TrivialBilling.setSkipUnauthorized(ctvSkipUnauthorized.isChecked());
            }
        }

        @Override
        public void onSetupStarted(@NonNull final SetupStartedEvent setupStartedEvent) {
            tvSetup.setVisibility(View.INVISIBLE);
            pgSetup.setVisibility(View.VISIBLE);
        }

        @Override
        public void onSetupResponse(@NonNull final SetupResponse setupResponse) {
            tvSetup.setVisibility(View.VISIBLE);
            pgSetup.setVisibility(View.INVISIBLE);
            if (setupResponse.isSuccessful()) {
                //noinspection ConstantConditions
                final BillingProviderInfo info = setupResponse.getBillingProvider().getInfo();
                final Provider provider = Provider.getByInfo(info);
                final String name = provider == null
                        ? info.getName()
                        : getString(provider.getNameId());
                final String authorized = getString(setupResponse.isAuthorized()
                                                            ? R.string.setup_authorized
                                                            : R.string.setup_unauthorized);
                tvSetup.setText(getString(R.string.label_setup_successful, name, authorized));
            } else {
                tvSetup.setText(R.string.label_setup_failed);
            }
        }
    }

    private class FooterViewHolder extends DragSortAdapter.ViewHolder
            implements View.OnClickListener {

        private final View btnAdd;

        public FooterViewHolder(final DragSortAdapter<?> dragSortAdapter, final View itemView) {
            super(dragSortAdapter, itemView);
            btnAdd = itemView.findViewById(R.id.btn_add);
            btnAdd.setOnClickListener(this);
        }

        @Override
        public void onClick(final View v) {
            final FragmentManager fragmentManager = getSupportFragmentManager();
            if (fragmentManager.findFragmentByTag(FRAGMENT_PROVIDER_PICKER) == null) {
                final Collection<Provider> providers = new ArrayList<>(
                        Arrays.asList(Provider.values()));
                providers.removeAll(adapter.items);
                final ProviderPickerDialogFragment dialogFragment = ProviderPickerDialogFragment
                        .getInstance(providers);
                dialogFragment.setListener(TrivialActivity.this);
                dialogFragment.show(fragmentManager, FRAGMENT_PROVIDER_PICKER);
            }
        }
    }

    private class ItemViewHolder extends DragSortAdapter.ViewHolder
            implements View.OnClickListener {

        private final View btnDelete;
        private final TextView tvProvider;
        private Provider provider;

        public ItemViewHolder(final DragSortAdapter<?> dragSortAdapter, final View itemView) {
            super(dragSortAdapter, itemView);
            btnDelete = itemView.findViewById(R.id.btn_delete);
            tvProvider = (TextView) itemView.findViewById(R.id.tv_provider);
            btnDelete.setOnClickListener(this);
            tvProvider.setOnClickListener(this);
        }

        public void setProvider(final Provider provider) {
            this.provider = provider;
            tvProvider.setText(provider.getNameId());
        }

        @Override
        public void onClick(final View v) {
            if (v == btnDelete) {
                adapter.deleteItem(provider);
            } else if (v == tvProvider) {
                startDrag();
            }
        }

        @Override
        public View.DragShadowBuilder getShadowBuilder(final View itemView,
                                                       final Point touchPoint) {
            return new DragSortShadowBuilder(itemView, touchPoint);
        }
    }

    private class HelpersAdapter extends ArrayAdapter<Helper> {

        private final LayoutInflater inflater;

        public HelpersAdapter() {
            super(TrivialActivity.this, -1, Helper.values());
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            inflater = getLayoutInflater();
        }

        @Override
        public View getView(final int position, final View convertView, final ViewGroup parent) {
            final View view;
            if (convertView != null) {
                view = convertView;
            } else {
                view = inflater.inflate(android.R.layout.simple_spinner_dropdown_item, parent,
                                        false);
            }
            final TextView textView = (TextView) view.findViewById(android.R.id.text1);
            textView.setText(getItem(position).getNameId());
            return view;
        }
    }
}
