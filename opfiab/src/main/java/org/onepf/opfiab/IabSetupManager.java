/*
 * Copyright 2012-2014 One Platform Foundation
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

import android.content.Context;
import android.support.annotation.NonNull;

import org.onepf.opfiab.model.Configuration;
import org.onepf.opfutils.Checkable;
import org.onepf.opfutils.OPFChecks;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import de.greenrobot.event.EventBus;

final class IabSetupManager {

    private static enum State {

        INITIAL,
        STARTED,
        FINISHED,
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(IabSetupManager.class);

    private static final String KEY_LAST_PROVIDER = "last_provider";

    private static final Checkable CHECK_INIT = new Checkable() {
        @Override
        public boolean check() {
            return instance == null;
        }
    };

    private static IabSetupManager instance;

    static void init(@NonNull final Context context, @NonNull final Configuration configuration) {
        OPFChecks.checkThread(true);
        OPFChecks.checkInit(CHECK_INIT, false);
        instance = new IabSetupManager(context, configuration);
    }

    static void setup() {
        OPFChecks.checkThread(true);
        OPFChecks.checkInit(CHECK_INIT, true);
    }


    @NonNull
    private final EventBus eventBus = OPFIab.getEventBus();

    @NonNull
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    @NonNull
    private volatile State state = State.INITIAL;

    @NonNull
    private final Context context;

    @NonNull
    private final Configuration configuration;

    private IabSetupManager(@NonNull final Context context,
                            @NonNull final Configuration configuration) {
        this.context = context;
        this.configuration = configuration;
    }

    private void startSetup() {

        if (state == State.FINISHED) {
            LOGGER.debug("Setup already finished. Skipping...");
            return;
        }

    }

    private void setupAsync() {

    }
}
