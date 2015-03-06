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

package org.onepf.opfiab;

import android.support.annotation.NonNull;

import org.onepf.opfiab.billing.BillingProvider;
import org.onepf.opfiab.model.Configuration;
import org.onepf.opfiab.model.event.SetupRequest;
import org.onepf.opfiab.model.event.SetupResponse;
import org.onepf.opfutils.OPFChecks;
import org.onepf.opfutils.OPFPreferences;

import java.util.Collection;
import java.util.LinkedHashSet;

import static org.onepf.opfiab.model.event.SetupResponse.Status.FAILED;
import static org.onepf.opfiab.model.event.SetupResponse.Status.SUCCESS;
import static org.onepf.opfiab.model.event.SetupResponse.Status.UNAUTHORISED;

final class SetupManager {

    private static final String KEY_LAST_PROVIDER = "last_provider";
    private static final OPFPreferences PREFERENCES = new OPFPreferences(OPFIab.getContext());

    static enum State {
        INITIAL,
        PROGRESS,
        FINISHED,
    }


    private State state = State.INITIAL;

    SetupManager() {
        super();
    }

    @NonNull
    private Iterable<BillingProvider> getAvailableProviders(
            @NonNull final Iterable<BillingProvider> providers) {
        final Collection<BillingProvider> availableProviders = new LinkedHashSet<>();
        for (final BillingProvider provider : providers) {
            if (provider.isAvailable()) {
                availableProviders.add(provider);
            }
        }
        return availableProviders;
    }

    @NonNull
    private SetupResponse newResponse(@NonNull final SetupRequest setupRequest) {
        //TODO utilize shared preferences
        final Configuration configuration = setupRequest.getConfiguration();
        for (final BillingProvider provider : getAvailableProviders(configuration.getProviders())) {
            final boolean authorised = provider.isAuthorised();
            if (authorised || !configuration.skipUnauthorised()) {
                return new SetupResponse(authorised ? SUCCESS : UNAUTHORISED, provider);
            }
        }
        return new SetupResponse(FAILED, null);
    }

    public void onEvent(@NonNull final SetupRequest setupRequest) {
        OPFChecks.checkThread(true);
        if (state == State.PROGRESS) {
            OPFIab.cancelEventDelivery(setupRequest);
        } else {
            state = State.PROGRESS;
        }
    }

    @SuppressWarnings("UnusedParameters")
    public void onEventMainThread(@NonNull final SetupResponse setupResponse) {
        state = State.FINISHED;
    }

    public void onEventAsync(@NonNull final SetupRequest setupRequest) {
        final SetupResponse setupResponse = newResponse(setupRequest);
        OPFIab.post(setupResponse);
    }
}
