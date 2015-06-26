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
import android.support.annotation.Nullable;

import org.onepf.opfutils.OPFUtils;

/**
 * Event indicating {@link Activity#onActivityResult(int, int, Intent)} call.
 */
public class ActivityResult extends ActivityEvent {

    private final int requestCode;
    private final int resultCode;
    @Nullable
    private final Intent data;

    public ActivityResult(@NonNull final Activity activity,
                          final int requestCode,
                          final int resultCode,
                          @Nullable final Intent data) {
        super(activity);
        this.requestCode = requestCode;
        this.resultCode = resultCode;
        this.data = data;
    }

    /**
     * Gets request code associated with this event.
     *
     * @return Request code.
     */
    public int getRequestCode() {
        return requestCode;
    }

    /**
     * Gets result code associated with this event.
     *
     * @return Result code.
     */
    public int getResultCode() {
        return resultCode;
    }

    /**
     * Gets data associated with this event.
     *
     * @return Data intent, can be null.
     */
    @Nullable
    public Intent getData() {
        return data;
    }

    //CHECKSTYLE:OFF
    @Override
    public String toString() {
        return "ActivityResult{" +
                "requestCode=" + requestCode +
                ", resultCode=" + resultCode +
                ", data=" + OPFUtils.toString(data) +
                '}';
    }
    //CHECKSTYLE:ON
}
