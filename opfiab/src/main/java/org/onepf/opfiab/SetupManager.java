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

import org.onepf.opfiab.billing.BillingController;
import org.onepf.opfiab.model.Configuration;
import org.onepf.opfiab.model.event.SetupRequest;
import org.onepf.opfiab.model.event.SetupResponse;
import org.onepf.opfutils.OPFPreferences;

import java.util.LinkedHashSet;
import java.util.Set;

import static org.onepf.opfiab.model.event.SetupResponse.Status.FAILED;
import static org.onepf.opfiab.model.event.SetupResponse.Status.SUCCESS;
import static org.onepf.opfiab.model.event.SetupResponse.Status.UNAUTHORISED;

class SetupManager {

    private static final String KEY_LAST_PROVIDER = "last_provider";
    private static final OPFPreferences PREFERENCES = new OPFPreferences(OPFIab.getContext());

    SetupManager() { }

    @NonNull
    private Set<BillingProvider> getAvailableProviders(
            @NonNull final Set<BillingProvider> providers) {
        final Set<BillingProvider> availableProviders = new LinkedHashSet<>();
        for (final BillingProvider provider : providers) {
            if (provider.isAvailable()) {
                availableProviders.add(provider);
            }
        }
        return availableProviders;
    }

    @NonNull
    private SetupResponse makeResponse(@NonNull final SetupRequest setupRequest) {
        //TODO utilize shared preferences
        final Configuration configuration = setupRequest.getConfiguration();
        for (final BillingProvider provider : getAvailableProviders(configuration.getProviders())) {
            final BillingController controller = provider.getController();
            final boolean authorised = controller.isAuthorised();
            if (!authorised && configuration.skipUnauthorised()) {
                continue;
            }
            if (controller.isBillingSupported()) {
                return new SetupResponse(authorised ? SUCCESS : UNAUTHORISED, provider);
            }
        }
        return new SetupResponse(FAILED, null);
    }

    public void onEventAsync(@NonNull final SetupRequest setupRequest) {
        final SetupResponse setupResponse = makeResponse(setupRequest);
        OPFIab.postSticky(setupResponse);
        OPFIab.removeStickyEvent(SetupRequest.class);
    }
}
