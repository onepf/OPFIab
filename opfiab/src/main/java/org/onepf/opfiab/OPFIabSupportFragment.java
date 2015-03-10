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

package org.onepf.opfiab;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.onepf.opfiab.model.event.ActivityResultEvent;
import org.onepf.opfiab.model.event.SupportFragmentLifecycleEvent;

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

public class OPFIabSupportFragment extends Fragment {

    @NonNull
    public static OPFIabSupportFragment newInstance() {
        return new OPFIabSupportFragment();
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        OPFIab.post(new SupportFragmentLifecycleEvent(ATTACH, this));
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        OPFIab.post(new SupportFragmentLifecycleEvent(CREATE, this));
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             final Bundle savedInstanceState) {
        final View view = super.onCreateView(inflater, container, savedInstanceState);
        OPFIab.post(new SupportFragmentLifecycleEvent(CREATE_VIEW, this));
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        OPFIab.post(new SupportFragmentLifecycleEvent(START, this));
    }

    @Override
    public void onResume() {
        super.onResume();
        OPFIab.post(new SupportFragmentLifecycleEvent(RESUME, this));
    }

    @Override
    public void onPause() {
        OPFIab.post(new SupportFragmentLifecycleEvent(PAUSE, this));
        super.onPause();
    }

    @Override
    public void onStop() {
        OPFIab.post(new SupportFragmentLifecycleEvent(STOP, this));
        super.onStop();
    }

    @Override
    public void onDestroyView() {
        OPFIab.post(new SupportFragmentLifecycleEvent(DESTROY_VIEW, this));
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        OPFIab.post(new SupportFragmentLifecycleEvent(DESTROY, this));
        super.onDestroy();
    }

    @Override
    public void onDetach() {
        OPFIab.post(new SupportFragmentLifecycleEvent(DETACH, this));
        super.onDetach();
    }

    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        OPFIab.post(new ActivityResultEvent(getActivity(), requestCode, resultCode, data));
    }
}
