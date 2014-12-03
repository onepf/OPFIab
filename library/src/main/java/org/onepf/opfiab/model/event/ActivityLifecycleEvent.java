/*
 * Copyright 2012-2014 One Platform Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.onepf.opfiab.model.event;

import android.app.Activity;
import android.support.annotation.NonNull;

import java.util.Arrays;

import static org.onepf.opfiab.model.event.LifecycleEvent.Type.CREATE;
import static org.onepf.opfiab.model.event.LifecycleEvent.Type.DESTROY;
import static org.onepf.opfiab.model.event.LifecycleEvent.Type.PAUSE;
import static org.onepf.opfiab.model.event.LifecycleEvent.Type.RESUME;
import static org.onepf.opfiab.model.event.LifecycleEvent.Type.START;
import static org.onepf.opfiab.model.event.LifecycleEvent.Type.STOP;

/**
 * Created by rzhilich on 12/3/14.
 */
public class ActivityLifecycleEvent extends LifecycleEvent {

    @NonNull
    private final Activity activity;

    public ActivityLifecycleEvent(@NonNull final Type type, @NonNull final Activity activity) {
        super(type);
        if (!Arrays.asList(CREATE, START, RESUME, PAUSE, STOP, DESTROY).contains(type)) {
            throw new IllegalArgumentException("Illegal lifecycle callback for Activity");
        }
        this.activity = activity;
    }

    @NonNull
    public Activity getActivity() {
        return activity;
    }
}
