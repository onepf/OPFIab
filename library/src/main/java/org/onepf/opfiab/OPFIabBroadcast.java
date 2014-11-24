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

package org.onepf.opfiab;

import android.content.Context;
import android.content.IntentFilter;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;

import org.onepf.opfiab.broadcast.OPFIabReceiver;
import org.onepf.opfiab.util.OPFUtils;

public class OPFIabBroadcast {

    private static LocalBroadcastManager localBroadcastManager;

    @NonNull
    private static final IntentFilter INTENT_FILTER = new IntentFilter();

    static {
        for (final OPFIabAction action : OPFIabAction.values()) {
            INTENT_FILTER.addAction(action.toString());
        }
    }

    static void init(@NonNull final Context context) {
        OPFUtils.checkThread(true);
        if (localBroadcastManager != null) {
            throw OPFUtils.alreadyInitException();
        }
        localBroadcastManager = LocalBroadcastManager.getInstance(context);
    }

    private static void checkInit() {
        if (localBroadcastManager == null) {
            throw OPFUtils.notInitException();
        }
    }


    public static void register(@NonNull final OPFIabReceiver receiver) {
        checkInit();
        localBroadcastManager.registerReceiver(receiver, INTENT_FILTER);
    }

    public static void unregister(@NonNull final OPFIabReceiver receiver) {
        checkInit();
        localBroadcastManager.unregisterReceiver(receiver);
    }

}
