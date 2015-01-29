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

import android.support.annotation.NonNull;

import org.onepf.opfiab.model.Configuration;
import org.onepf.opfiab.model.event.SetupEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;

import static org.onepf.opfiab.model.event.SetupEvent.Status.FAILED;
import static org.onepf.opfiab.model.event.SetupEvent.Status.SUCCESS;

final class SetupManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(SetupManager.class);

    private static final String KEY_LAST_PROVIDER = "last_provider";

    static enum State {

        INITIAL,
        STARTED,
        FINISHED,
    }


    @NonNull
    private State state = State.INITIAL;

    //TODO stub implementation
    void setup() {
        final Configuration configuration = OPFIab.getConfiguration();
        final Iterator<BillingProvider> iterator = configuration.getProviders().iterator();
        final BillingProvider billingProvider = iterator.hasNext() ? iterator.next() : null;

        final SetupEvent event;
        if (billingProvider == null || !billingProvider.isAvailable()) {
            event = new SetupEvent(FAILED, null);
        } else {
            event = new SetupEvent(SUCCESS, billingProvider);
        }
        OPFIab.getEventBus().postSticky(event);
        state = State.FINISHED;
    }

    @NonNull
    State getState() {
        return state;
    }
}
