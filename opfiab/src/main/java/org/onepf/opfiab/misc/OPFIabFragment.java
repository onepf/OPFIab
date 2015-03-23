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

package org.onepf.opfiab.misc;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.onepf.opfiab.OPFIab;
import org.onepf.opfiab.model.event.android.FragmentLifecycleEvent;

import static org.onepf.opfiab.model.ComponentState.ATTACH;
import static org.onepf.opfiab.model.ComponentState.CREATE;
import static org.onepf.opfiab.model.ComponentState.CREATE_VIEW;
import static org.onepf.opfiab.model.ComponentState.DESTROY;
import static org.onepf.opfiab.model.ComponentState.DESTROY_VIEW;
import static org.onepf.opfiab.model.ComponentState.DETACH;
import static org.onepf.opfiab.model.ComponentState.PAUSE;
import static org.onepf.opfiab.model.ComponentState.RESUME;
import static org.onepf.opfiab.model.ComponentState.START;
import static org.onepf.opfiab.model.ComponentState.STOP;

public class OPFIabFragment extends Fragment {

    @NonNull
    public static OPFIabFragment newInstance() {
        return new OPFIabFragment();
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        OPFIab.post(new FragmentLifecycleEvent(ATTACH, this));
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        OPFIab.post(new FragmentLifecycleEvent(CREATE, this));
    }

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             final Bundle savedInstanceState) {
        final View view = super.onCreateView(inflater, container, savedInstanceState);
        OPFIab.post(new FragmentLifecycleEvent(CREATE_VIEW, this));
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        OPFIab.post(new FragmentLifecycleEvent(START, this));
    }

    @Override
    public void onResume() {
        super.onResume();
        OPFIab.post(new FragmentLifecycleEvent(RESUME, this));
    }

    @Override
    public void onPause() {
        OPFIab.post(new FragmentLifecycleEvent(PAUSE, this));
        super.onPause();
    }

    @Override
    public void onStop() {
        OPFIab.post(new FragmentLifecycleEvent(STOP, this));
        super.onStop();
    }

    @Override
    public void onDestroyView() {
        OPFIab.post(new FragmentLifecycleEvent(DESTROY_VIEW, this));
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        OPFIab.post(new FragmentLifecycleEvent(DESTROY, this));
        super.onDestroy();
    }

    @Override
    public void onDetach() {
        OPFIab.post(new FragmentLifecycleEvent(DETACH, this));
        super.onDetach();
    }
}
