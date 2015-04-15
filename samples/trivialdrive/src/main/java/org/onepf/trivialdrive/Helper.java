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

package org.onepf.trivialdrive;

import android.app.Activity;
import android.support.annotation.StringRes;

import org.onepf.trivialdrive.R;
import org.onepf.trivialdrive.ui.activity.ActivityHelperActivity;
import org.onepf.trivialdrive.ui.activity.FragmentHelperActivity;
import org.onepf.trivialdrive.ui.activity.AdvancedHelperActivity;

public enum Helper {

    ACTIVITY(R.string.helper_activity, ActivityHelperActivity.class),
    FRAGMENT(R.string.helper_fragment, FragmentHelperActivity.class),
    ADVANCED(R.string.helper_advanced, AdvancedHelperActivity.class),;

    @StringRes
    private final int nameId;
    private final Class<? extends Activity> activityClass;

    Helper(final int nameId, final Class<? extends Activity> activityClass) {
        this.nameId = nameId;
        this.activityClass = activityClass;
    }

    public int getNameId() {
        return nameId;
    }

    public Class<? extends Activity> getActivityClass() {
        return activityClass;
    }
}
