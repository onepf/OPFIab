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
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;

import org.onepf.opfiab.model.event.ActivityResultEvent;
import org.onepf.opfiab.model.event.FragmentLifecycleEvent;

import static org.onepf.opfiab.model.ComponentState.ATTACH;
import static org.onepf.opfiab.model.ComponentState.CREATE;
import static org.onepf.opfiab.model.ComponentState.DESTROY;
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
        setRetainInstance(false);
        OPFIab.post(new FragmentLifecycleEvent(CREATE, this));
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
    public void onDetach() {
        OPFIab.post(new FragmentLifecycleEvent(DETACH, this));
        super.onDetach();
    }

    @Override
    public void onDestroy() {
        OPFIab.post(new FragmentLifecycleEvent(DESTROY, this));
        super.onDestroy();
    }

    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        OPFIab.post(new ActivityResultEvent(getActivity(), requestCode, resultCode, data));
    }
}
