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
 * This class with try to pick one {@link BillingProvider} from those available from
 * {@link Configuration#getProviders()}.
 * <br>
 * Provider will be picked according to this priority:
 * <ul>
 * <li> Only available providers will be considered, according to {@link BillingProvider#isAvailable()}.
 * <li> If was already used by this app, it will be considered first.
 * <li> If provider has {@link BillingProviderInfo#getInstaller()} that matches this application
 * package installer, it will be considered next.
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
    private boolean setupInProgress;
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
            final SetupResponse.Status status = providerChanged ? PROVIDER_CHANGED : SUCCESS;
            return new SetupResponse(configuration, status, billingProvider, authorized);
        }
        OPFLog.d(String.format("%s does not satisfies configuration (skipUnauthorised = %b)", billingProvider.getInfo().getName(), configuration.skipUnauthorised()));
        return null;
    }

    @NonNull
    private SetupResponse newResponse(@NonNull final SetupStartedEvent setupStartedEvent) {
        OPFLog.logMethod(setupStartedEvent);

        final Configuration configuration = setupStartedEvent.getConfiguration();
        final Iterable<BillingProvider> providers = configuration.getProviders();
        final Iterable<BillingProvider> availableProviders = OPFIabUtils.getAvailable(providers);

        final boolean hadProvider = preferences.contains(KEY_LAST_PROVIDER);
        // Try previously used provider
        if (hadProvider) {
            final String lastProvider = preferences.getString(KEY_LAST_PROVIDER, "");
            final BillingProviderInfo info = BillingProviderInfo.fromJson(lastProvider);
            final BillingProvider provider;
            final SetupResponse setupResponse;
            OPFLog.d("Previous provider: " + lastProvider);
            if (info != null
                    && (provider = OPFIabUtils.findWithInfo(availableProviders, info)) != null
                    && (setupResponse = withProvider(configuration, provider, false)) != null) {
                return setupResponse;
            }
        }

        final String packageInstaller = OPFUtils.getPackageInstaller(context);
        OPFLog.d("Package installer: " + packageInstaller);
        // If package installer is set, try it before anything else
        if (!TextUtils.isEmpty(packageInstaller)) {
            final BillingProvider installerProvider = OPFIabUtils
                    .withInstaller(availableProviders, packageInstaller);
            final SetupResponse setupResponse;
            if (installerProvider != null
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

        return new SetupResponse(configuration, FAILED, null);
    }

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
            final BillingProvider provider = setupResponse.getBillingProvider();
            //noinspection ConstantConditions
            final BillingProviderInfo info = provider.getInfo();
            preferences.put(KEY_LAST_PROVIDER, info.toJson().toString());
        }
        OPFIab.post(setupResponse);
    }
}
