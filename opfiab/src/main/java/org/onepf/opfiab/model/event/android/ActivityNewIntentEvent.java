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
import android.content.Intent;
import android.support.annotation.NonNull;

/**
 * Event indicating {@link Activity#onNewIntent(Intent)} call.
 */
public class ActivityNewIntentEvent extends ActivityEvent {

    @NonNull
    private final Intent intent;

    public ActivityNewIntentEvent(@NonNull final Activity activity,
                                  @NonNull final Intent intent) {
        super(activity);
        this.intent = intent;
    }

    /**
     * Gets intent associated with this event.
     *
     * @return Intent object, can't be null.
     */
    @NonNull
    public Intent getIntent() {
        return intent;
    }
}
