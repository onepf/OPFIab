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

package org.onepf.trivialdrive.ui.activity;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Point;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
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
import org.onepf.opfiab.trivialdrive.R;
import org.onepf.opfutils.OPFPreferences;
import org.onepf.trivialdrive.Helper;
import org.onepf.trivialdrive.OnProviderPickerListener;
import org.onepf.trivialdrive.Provider;
import org.onepf.trivialdrive.TrivialBilling;
import org.onepf.trivialdrive.ui.fragment.ProviderPickerDialogFragment;
import org.onepf.trivialdrive.ui.view.TrivialView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

abstract class TrivialActivity extends ActionBarActivity
        implements OnProviderPickerListener {

    private static final String FRAGMENT_PROVIDER_PICKER = "provider_picker";


    private AdvancedIabHelper iabHelper;

    private Toolbar toolbar;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle drawerToggle;
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
    protected void onDestroy() {
        iabHelper.unregister();
        super.onDestroy();
    }

    @Override
    public void setContentView(final int layoutResID) {
        super.setContentView(R.layout.activity_trivial);
        toolbar = (Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer);
        final View view = getLayoutInflater().inflate(layoutResID, drawerLayout, false);
        drawerLayout.addView(view, 0);
        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, 0, 0);
        drawerLayout.setDrawerListener(drawerToggle);

        recyclerView = (RecyclerView) findViewById(R.id.recycler);
        adapter = new Adapter(recyclerView);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    protected void onPostCreate(final Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        drawerToggle.syncState();
        if (savedInstanceState == null) {
            drawerLayout.openDrawer(Gravity.LEFT);
        }
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        if (drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onConfigurationChanged(final Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        drawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public void onProviderPicked(final Provider provider) {
        adapter.addItem(provider);
    }

    private class Adapter extends DragSortAdapter<DragSortAdapter.ViewHolder> {

        private static final int TYPE_HEADER = -1;
        private static final int TYPE_FOOTER = -2;
        private static final int TYPE_ITEM = 0;

        private final int headerCount = 1;

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

        private void addItem(final Provider provider) {
            if (items.contains(provider)) {
                return;
            }
            items.add(provider);
            TrivialBilling.setProviders(items);
            notifyItemInserted(headerCount + items.size());
        }

        private void deleteItem(final Provider provider) {
            final int index;
            if ((index = items.indexOf(provider)) < 0) {
                return;
            }
            items.remove(index);
            TrivialBilling.setProviders(items);
            notifyItemRemoved(headerCount + index);
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
                final Provider provider = items.get(position - headerCount);
                viewHolder.setProvider(provider);
                final boolean isDragged = getDraggingId() == getItemId(position);
                viewHolder.itemView.setVisibility(isDragged ? INVISIBLE : VISIBLE);
            }
        }

        @Override
        public int getItemCount() {
            return headerCount + items.size() + (showFooter() ? 1 : 0);
        }

        @Override
        public long getItemId(final int position) {
            switch (getItemViewType(position)) {
                case TYPE_HEADER:
                    return TYPE_HEADER;
                case TYPE_FOOTER:
                    return TYPE_FOOTER;
                default:
                    return items.get(position - headerCount).ordinal();
            }
        }

        @Override
        public int getItemViewType(final int position) {
            if (position == 0) {
                return TYPE_HEADER;
            }
            if (position >= headerCount + items.size()) {
                return TYPE_FOOTER;
            }
            return TYPE_ITEM;
        }

        @Override
        public int getPositionForId(final long l) {
            switch ((int) l) {
                case TYPE_HEADER:
                    return 0;
                case TYPE_FOOTER:
                    return items.size();
                default:
                    return headerCount + items.indexOf(Provider.values()[(int) l]);
            }
        }

        @Override
        public boolean move(final int from, final int to) {
            if (getItemViewType(to) != TYPE_ITEM) {
                return false;
            }
            Collections.swap(items, from - headerCount, to - headerCount);
            TrivialBilling.setProviders(items);
            return true;
        }
    }

    private class HeaderViewHolder extends DragSortAdapter.ViewHolder
            implements View.OnClickListener, OnSetupListener, AdapterView.OnItemSelectedListener {

        private final Spinner spinHelper;
        private final TextView tvSetupStatus;
        private final TextView tvSetupProvider;
        private final TextView tvSetupAuthorisation;
        private final ProgressBar pbSetup;
        private final Button btnForget;
        private final Button btnInit;
        private final Button btnSetup;
        private final CheckedTextView ctvSkipUnauthorized;
        private final CheckedTextView ctvAutoRecover;

        public HeaderViewHolder(final DragSortAdapter<?> dragSortAdapter, final View itemView) {
            super(dragSortAdapter, itemView);
            spinHelper = (Spinner) itemView.findViewById(R.id.spin_helper);
            tvSetupStatus = (TextView) itemView.findViewById(R.id.tv_setup_status);
            tvSetupProvider = (TextView) itemView.findViewById(R.id.tv_setup_provider);
            tvSetupAuthorisation = (TextView) itemView.findViewById(R.id.tv_setup_authorisation);
            pbSetup = (ProgressBar) itemView.findViewById(R.id.pb_setup);
            btnForget = (Button) itemView.findViewById(R.id.btn_forget);
            btnInit = (Button) itemView.findViewById(R.id.btn_init);
            btnSetup = (Button) itemView.findViewById(R.id.btn_setup);
            ctvAutoRecover = (CheckedTextView) itemView.findViewById(R.id.ctv_auto_recover);
            ctvSkipUnauthorized = (CheckedTextView) itemView
                    .findViewById(R.id.ctv_skip_unauthorized);

            final HelpersAdapter adapter = new HelpersAdapter();
            spinHelper.setAdapter(adapter);
            spinHelper.setSelection(adapter.getPosition(TrivialBilling.getHelper()));
            spinHelper.setOnItemSelectedListener(this);

            btnForget.setOnClickListener(this);
            btnInit.setOnClickListener(this);
            btnSetup.setOnClickListener(this);
            ctvAutoRecover.setChecked(TrivialBilling.isAutoRecover());
            ctvAutoRecover.setOnClickListener(this);
            ctvSkipUnauthorized.setOnClickListener(this);
            ctvSkipUnauthorized.setChecked(TrivialBilling.isSkipUnauthorized());

            iabHelper.addSetupListener(this);
        }


        private void setSetupResponse(final SetupResponse setupResponse) {
            pbSetup.setVisibility(INVISIBLE);
            tvSetupProvider.setVisibility(VISIBLE);
            final boolean setupSuccessful = setupResponse != null && setupResponse.isSuccessful();
            final int visibility = setupSuccessful ? VISIBLE : INVISIBLE;
            tvSetupStatus.setVisibility(visibility);
            tvSetupAuthorisation.setVisibility(visibility);
            if (setupSuccessful) {
                //noinspection ConstantConditions
                final BillingProviderInfo info = setupResponse.getBillingProvider().getInfo();
                final Provider provider = Provider.getByInfo(info);
                tvSetupProvider.setText(provider == null
                                                ? info.getName()
                                                : getString(provider.getNameId()));
                tvSetupStatus.setText(setupResponse.getStatus().toString());
                tvSetupAuthorisation.setText(setupResponse.isAuthorized()
                                                     ? R.string.setup_authorized
                                                     : R.string.setup_unauthorized);
            } else {
                tvSetupProvider.setText(R.string.setup_no_provider);
            }
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
            if (v == btnForget) {
                // strictly for demo purposes
                // you probably shouldn't mess with OPFPreferences in real app
                new OPFPreferences(getApplicationContext()).clear();
            } else if (v == btnInit) {
                final TrivialActivity context = TrivialActivity.this;
                OPFIab.init(getApplication(), TrivialBilling.getRelevantConfiguration(context));
                setSetupResponse(null);
                // strictly for demo purposes
                TrivialBilling.updateSetup();
                final TrivialView trivialView = (TrivialView) findViewById(R.id.trivial);
                trivialView.updatePremium();
                trivialView.updateSubscription();
                trivialView.updateSkuDetails();
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
            pbSetup.setVisibility(VISIBLE);
            tvSetupStatus.setVisibility(INVISIBLE);
            tvSetupProvider.setVisibility(INVISIBLE);
            tvSetupAuthorisation.setVisibility(INVISIBLE);
        }

        @Override
        public void onSetupResponse(@NonNull final SetupResponse setupResponse) {
            setSetupResponse(setupResponse);
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
            implements View.OnClickListener, View.OnLongClickListener {

        private final View btnDelete;
        private final TextView tvProvider;
        private Provider provider;

        public ItemViewHolder(final DragSortAdapter<?> dragSortAdapter, final View itemView) {
            super(dragSortAdapter, itemView);
            btnDelete = itemView.findViewById(R.id.btn_delete);
            tvProvider = (TextView) itemView.findViewById(R.id.tv_provider);
            btnDelete.setOnClickListener(this);
            tvProvider.setOnLongClickListener(this);
        }

        public void setProvider(final Provider provider) {
            this.provider = provider;
            tvProvider.setText(provider.getNameId());
        }

        @Override
        public void onClick(final View v) {
            adapter.deleteItem(provider);
        }

        @Override
        public boolean onLongClick(final View v) {
            startDrag();
            return true;
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
