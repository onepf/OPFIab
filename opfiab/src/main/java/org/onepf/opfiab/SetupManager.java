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
import android.support.annotation.Nullable;

import org.onepf.opfiab.billing.BillingController;
import org.onepf.opfiab.model.Configuration;
import org.onepf.opfiab.model.event.SetupEvent;
import org.onepf.opfutils.OPFChecks;
import org.onepf.opfutils.OPFPreferences;
import org.onepf.opfutils.OPFUtils;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;

import de.greenrobot.event.EventBus;

import static org.onepf.opfiab.model.event.SetupEvent.Status.FAILED;
import static org.onepf.opfiab.model.event.SetupEvent.Status.SUCCESS;

final class SetupManager {

    static enum State {

        INITIAL,
        STARTED,
        FINISHED,
    }

    private static final OPFPreferences PREFERENCES = new OPFPreferences(OPFIab.getContext());
    private static final String KEY_LAST_PROVIDER = "last_provider";

    private static final Configuration CONFIGURATION = OPFIab.getConfiguration();
    private static final ExecutorService EXECUTOR = OPFIab.getExecutor();
    private static final EventBus EVENT_BUS = OPFIab.getEventBus();

    @NonNull
    private static volatile State state = State.INITIAL;

    private SetupManager() {
        throw new UnsupportedOperationException();
    }

    @NonNull
    private static Collection<BillingProvider> getAvailableProviders() {
        final Set<BillingProvider> availableProviders = new LinkedHashSet<>();
        for (final BillingProvider provider : CONFIGURATION.getProviders()) {
            if (provider.isAvailable()) {
                availableProviders.add(provider);
            }
        }
        return availableProviders;
    }

    @Nullable
    private static BillingProvider pickProvider(
            @NonNull final Collection<BillingProvider> providers) {
        //TODO stub implementation
        for (final BillingProvider provider : providers) {
            final BillingController controller = provider.getController();
            if (CONFIGURATION.skipUnauthorised() && !controller.isAuthorised()) {
                continue;
            }
            if (controller.isBillingSupported()) {
                return provider;
            }
        }
        return null;
    }

    static void setup() {
        OPFChecks.checkThread(true);
        if (state == State.STARTED) {
            return;
        }
        state = State.STARTED;
        EVENT_BUS.removeStickyEvent(SetupEvent.class);
        EXECUTOR.execute(new Runnable() {
            @Override
            public void run() {
                final BillingProvider provider = pickProvider(getAvailableProviders());
                final SetupEvent.Status status = provider == null ? FAILED : SUCCESS;
                final SetupEvent setupEvent = new SetupEvent(status, provider);
                OPFUtils.post(new Runnable() {
                    @Override
                    public void run() {
                        state = State.FINISHED;
                        EVENT_BUS.postSticky(setupEvent);
                    }
                });
            }
        });
    }

    @NonNull
    static State getState() {
        return state;
    }
}
