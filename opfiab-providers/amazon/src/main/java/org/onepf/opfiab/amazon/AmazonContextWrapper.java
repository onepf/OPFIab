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

package org.onepf.opfiab.amazon;

import android.content.ComponentName;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.ResolveInfo;

import java.util.List;

public class AmazonContextWrapper extends ContextWrapper {

    public AmazonContextWrapper(final Context base) {
        super(base.getApplicationContext());
    }

    @Override
    public Context getApplicationContext() {
        return this;
    }

    @Override
    public ComponentName startService(final Intent intent) {
        final List<ResolveInfo> infos = getPackageManager().queryIntentServices(intent, 0);
        if (infos == null || infos.isEmpty()) {
            return super.startService(intent);
        }
        final ResolveInfo serviceInfo = infos.get(0);
        final String packageName = serviceInfo.serviceInfo.packageName;
        final String className = serviceInfo.serviceInfo.name;
        final ComponentName component = new ComponentName(packageName, className);
        final Intent explicitIntent = new Intent(intent);
        explicitIntent.setComponent(component);
        return super.startService(explicitIntent);
    }
}
