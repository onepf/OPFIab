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
import android.text.TextUtils;

import org.onepf.opfiab.billing.BillingProvider;
import org.onepf.opfiab.model.BillingProviderInfo;
import org.onepf.opfiab.model.Configuration;
import org.onepf.opfiab.model.Configuration.Builder;
import org.onepf.opfiab.model.event.SetupResponse;
import org.onepf.opfiab.model.event.SetupStartedEvent;
import org.onepf.opfiab.util.OPFIabUtils;
import org.onepf.opfutils.OPFChecks;
import org.onepf.opfutils.OPFLog;
import org.onepf.opfutils.OPFPreferences;
import org.onepf.opfutils.OPFUtils;

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
 * <li> If provider had been already used by this app, it is considered first.
 * <li> If provider has {@link BillingProviderInfo#getInstaller()} that matches this application
 * package installer, it is considered next.
 * <li> First suitable provider will be picked according to order it was added in
 * {@link Builder#addBillingProvider(BillingProvider)}.
 * </ul>
 */
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


    private final Context context;
    private final OPFPreferences preferences;
    /**
     * Flag indicating whether setup process is happening at the moment.
     */
    private boolean setupInProgress;
    /**
     * Configuration object from last received setup request.
     * <br>
     * Used to determine whether {@link SetupResponse} is relevant when it's ready.
     */
    @Nullable
    private Configuration lastConfiguration;

    private SetupManager(@NonNull final Context context) {
        super();
        this.context = context.getApplicationContext();
        preferences = new OPFPreferences(context);
    }

    @Nullable
    private SetupResponse withProvider(@NonNull final Configuration configuration,
                                       @NonNull final BillingProvider billingProvider,
                                       final boolean providerChanged) {
        final boolean authorized = billingProvider.isAuthorised();
        OPFLog.d(billingProvider.getInfo().getName() + " isAuthorized = " + authorized);
        if (authorized || !configuration.skipUnauthorised()) {
            // Provider is authorized or we don't care about authorization
            final SetupResponse.Status status = providerChanged ? PROVIDER_CHANGED : SUCCESS;
            return new SetupResponse(configuration, status, billingProvider, authorized);
        }
        OPFLog.d("Skipping: %s", billingProvider);
        return null;
    }

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
            final BillingProviderInfo info = BillingProviderInfo.fromJson(lastProvider);
            final BillingProvider provider;
            final SetupResponse setupResponse;
            OPFLog.d("Previous provider: %s", lastProvider);
            if (info != null
                    // Last provider info is valid
                    && (provider = OPFIabUtils.findWithInfo(availableProviders, info)) != null
                    // Provider is present in configuration
                    && (setupResponse = withProvider(configuration, provider, false)) != null) {
                return setupResponse;
            }
        }

        final String packageInstaller = OPFUtils.getPackageInstaller(context);
        OPFLog.d("Package installer: %s", packageInstaller);
        if (!TextUtils.isEmpty(packageInstaller)) {
            // If package installer is set, try it before anything else
            final BillingProvider installerProvider = OPFIabUtils
                    .withInstaller(availableProviders, packageInstaller);
            final SetupResponse setupResponse;
            if (installerProvider != null
                    // Provider is present in configuration
                    && (setupResponse = withProvider(configuration, installerProvider,
                                                     hadProvider)) != null) {
                return setupResponse;
            }
        }

        // Pick first available provider that satisfies current configuration
        for (final BillingProvider provider : availableProviders) {
            final SetupResponse setupResponse = withProvider(configuration, provider, hadProvider);
            if (setupResponse != null) {
                return setupResponse;
            }
        }

        // No suitable provider was found
        return new SetupResponse(configuration, FAILED, null);
    }

    /**
     * Tries to start setup process for supplied configuration.
     * <br>
     * If setup is already in progress, new configuration object will be stored and used after
     * current setup is finished.
     *
     * @param configuration Configuration object to perform setup for.
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
        final BillingProvider provider;
        if (setupResponse.isSuccessful()
                && (provider = setupResponse.getBillingProvider()) != null) {
            // Suitable provider successfully picked, remember it to prioritize for next setup.
            final BillingProviderInfo info = provider.getInfo();
            preferences.put(KEY_LAST_PROVIDER, info.toJson().toString());
        }
        OPFIab.post(setupResponse);
    }
}
