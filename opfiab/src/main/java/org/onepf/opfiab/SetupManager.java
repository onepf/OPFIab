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
import org.onepf.opfiab.model.event.SetupRequest;
import org.onepf.opfiab.model.event.SetupResponse;
import org.onepf.opfiab.util.OPFIabUtils;
import org.onepf.opfutils.OPFChecks;
import org.onepf.opfutils.OPFPreferences;

import static org.onepf.opfiab.model.event.SetupResponse.Status.FAILED;
import static org.onepf.opfiab.model.event.SetupResponse.Status.PROVIDER_CHANGED;
import static org.onepf.opfiab.model.event.SetupResponse.Status.SUCCESS;

final class SetupManager {

    private static final String KEY_LAST_PROVIDER = SetupManager.class.getName() + ".last_provider";

    private static SetupManager instance;

    static enum State {
        INITIAL,
        PROGRESS,
        FINISHED,
    }

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
    @NonNull
    private State state = State.INITIAL;

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
        if (authorized || !configuration.skipUnauthorised()) {
            final SetupResponse.Status status = providerChanged ? PROVIDER_CHANGED : SUCCESS;
            return new SetupResponse(status, billingProvider, authorized);
        }
        return null;
    }

    @NonNull
    private SetupResponse newResponse(@NonNull final SetupRequest setupRequest) {
        final Configuration configuration = setupRequest.getConfiguration();
        final Iterable<BillingProvider> providers = configuration.getProviders();
        final Iterable<BillingProvider> availableProviders = OPFIabUtils.getAvailable(providers);

        final boolean hadProvider = preferences.contains(KEY_LAST_PROVIDER);
        // Try previously used provider
        if (hadProvider) {
            final String lastProvider = preferences.getString(KEY_LAST_PROVIDER, "");
            final BillingProviderInfo info = BillingProviderInfo.fromJson(lastProvider);
            final BillingProvider provider;
            final SetupResponse setupResponse;
            if (info != null
                    && (provider = OPFIabUtils.findWithInfo(availableProviders, info)) != null
                    && (setupResponse = withProvider(configuration, provider, false)) != null) {
                return setupResponse;
            }
        }

        final String packageInstaller = OPFIabUtils.getPackageInstaller(context);
        // If package installer is set, try it before anything else
        if (!TextUtils.isEmpty(packageInstaller)) {
            final BillingProvider installerProvider =
                    OPFIabUtils.withPackage(availableProviders, packageInstaller);
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

        return new SetupResponse(FAILED, null);
    }

    public void onEvent(@NonNull final SetupRequest setupRequest) {
        OPFChecks.checkThread(true);
        // Guaranteed to be called before any scheduled event delivery
        if (state == State.PROGRESS) {
            // Setup is in progress, halt setup request delivery
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
        if (setupResponse.isSuccessful()) {
            final BillingProvider provider = setupResponse.getBillingProvider();
            //noinspection ConstantConditions
            final BillingProviderInfo info = provider.getInfo();
            preferences.put(KEY_LAST_PROVIDER, info.toJson().toString());
        }
        OPFIab.post(setupResponse);
    }
}
