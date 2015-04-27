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

package org.onepf.opfiab.model.event.android;

import android.app.Activity;
import android.support.annotation.NonNull;

/**
 * Parent class for any activity related events.
 */
class ActivityEvent {

    @NonNull
    protected final Activity activity;

    protected ActivityEvent(@NonNull final Activity activity) {
        this.activity = activity;
    }

    @NonNull
    public Activity getActivity() {
        return activity;
    }
}
