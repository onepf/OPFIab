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

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.onepf.opfiab.billing.BillingProvider;
import org.onepf.opfiab.billing.Compatibility;
import org.onepf.opfiab.model.Configuration;
import org.onepf.opfiab.model.Configuration.Builder;
import org.onepf.opfiab.model.event.SetupResponse;
import org.onepf.opfiab.model.event.SetupStartedEvent;
import org.onepf.opfiab.util.OPFIabUtils;
import org.onepf.opfutils.OPFChecks;
import org.onepf.opfutils.OPFLog;
import org.onepf.opfutils.OPFPreferences;

import static org.onepf.opfiab.model.event.SetupResponse.Status.FAILED;
import static org.onepf.opfiab.model.event.SetupResponse.Status.PROVIDER_CHANGED;
import static org.onepf.opfiab.model.event.SetupResponse.Status.SUCCESS;

/**
 * This class tries to pick one {@link BillingProvider} from those available from
 * {@link Configuration#getProviders()}.
 * <br>
 * Providers are picked according to this priority rules:
 * <ul>
 * <li> Only available providers will be considered, according to {@link BillingProvider#isAvailable()}.
 * <li> If some provider had been already used by this app, it is considered first.
 * <li> If provider returns {@link Compatibility#PREFERRED} from
 * {@link BillingProvider#checkCompatibility()} it is considered next.
 * <li> First suitable provider will be picked according to order it was added in
 * {@link Builder#addBillingProvider(BillingProvider)}.
 * </ul>
 */
@SuppressWarnings({"PMD.ModifiedCyclomaticComplexity", "PMD.StdCyclomaticComplexity"})
final class SetupManager {

    private static final String KEY_LAST_PROVIDER = SetupManager.class.getName() + ".last_provider";

    private static SetupManager instance;

    @SuppressWarnings({"PMD.NonThreadSafeSingleton"})
    static SetupManager getInstance(@NonNull final Context context) {
        OPFChecks.checkThread(true);
        if (instance == null) {
            instance = new SetupManager(context);
        }
        return instance;
    }


    private final OPFPreferences preferences;
    /**
     * Flag indicating whether setup process is happening at the moment.
     */
    private boolean setupInProgress;
    /**
     * Configuration object from last received setup request.
     * <p/>
     * Used to determine whether {@link SetupResponse} is relevant when it's ready.
     */
    @Nullable
    private Configuration lastConfiguration;

    private SetupManager(@NonNull final Context context) {
        super();
        preferences = new OPFPreferences(context);
    }

    @SuppressWarnings({"PMD.NPathComplexity", "PMD.AvoidDeeplyNestedIfStmts"})
    @NonNull
    private SetupResponse newResponse(@NonNull final SetupStartedEvent setupStartedEvent) {
        OPFLog.logMethod(setupStartedEvent);

        final Configuration configuration = setupStartedEvent.getConfiguration();
        final Iterable<BillingProvider> providers = configuration.getProviders();
        final Iterable<BillingProvider> availableProviders = OPFIabUtils.getAvailable(providers);

        final boolean hadProvider = preferences.contains(KEY_LAST_PROVIDER);
        if (hadProvider) {
            // Try previously used provider
            final String lastProvider = preferences.getString(KEY_LAST_PROVIDER, "");
            OPFLog.d("Previous provider: %s", lastProvider);
            for (final BillingProvider provider : availableProviders) {
                if (lastProvider.equals(provider.getName())) {
                    // Use last provider if it's compatible.
                    if (provider.checkCompatibility() != Compatibility.INCOMPATIBLE) {
                        return new SetupResponse(configuration, SUCCESS, provider);
                    }
                    break;
                }
            }
        }

        // Use appropriate success status
        final SetupResponse.Status successStatus = hadProvider ? PROVIDER_CHANGED : SUCCESS;

        BillingProvider compatibleProvider = null;
        for (final BillingProvider provider : availableProviders) {
            final Compatibility compatibility = provider.checkCompatibility();
            OPFLog.d("Checking provider: %s, compatibility: %s", provider.getName(), compatibility);
            if (compatibility == Compatibility.PREFERRED) {
                // Pick preferred provider
                return new SetupResponse(configuration, successStatus, provider);
            } else if (compatibility == Compatibility.COMPATIBLE && compatibleProvider == null) {
                compatibleProvider = provider;
            }
        }

        // Pick first compatible provider
        if (compatibleProvider != null) {
            return new SetupResponse(configuration, successStatus, compatibleProvider);
        }

        // No suitable provider was found
        return new SetupResponse(configuration, FAILED, null);
    }

    /**
     * Tries to start setup process for the supplied configuration.
     * <br>
     * If setup is already in progress, new configuration object is stored and used after
     * current setup is finished.
     *
     * @param configuration Configuration object to perform setup for.
     *
     * @see OPFIab#setup()
     */
    void startSetup(@NonNull final Configuration configuration) {
        OPFChecks.checkThread(true);
        lastConfiguration = configuration;
        if (setupInProgress) {
            return;
        }

        setupInProgress = true;
        OPFIab.post(new SetupStartedEvent(configuration));
    }

    @SuppressWarnings("UnusedParameters")
    public void onEventMainThread(@NonNull final SetupResponse setupResponse) {
        setupInProgress = false;
        if (lastConfiguration != null && lastConfiguration != setupResponse.getConfiguration()) {
            // If another setup was requested with different configuration
            startSetup(lastConfiguration);
        } else {
            lastConfiguration = null;
        }
    }

    public void onEventAsync(@NonNull final SetupStartedEvent setupStartedEvent) {
        final SetupResponse setupResponse = newResponse(setupStartedEvent);
        if (setupResponse.isSuccessful()) {
            // Suitable provider successfully picked, save it for next setup.
            //noinspection ConstantConditions
            preferences.put(KEY_LAST_PROVIDER, setupResponse.getBillingProvider().getName());
        }
        OPFIab.post(setupResponse);
    }
}
