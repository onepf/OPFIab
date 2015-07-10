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

package org.onepf.opfiab.model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.onepf.opfiab.OPFIab;
import org.onepf.opfiab.billing.BillingProvider;
import org.onepf.opfiab.listener.BillingListener;
import org.onepf.opfiab.listener.DefaultBillingListener;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Model class representing library configuration.
 *
 * @see OPFIab#init(android.app.Application, Configuration)
 */
@SuppressWarnings("PMD.MissingStaticMethodInNonInstantiatableClass")
public final class Configuration {

    @NonNull
    private final Set<BillingProvider> providers;
    @Nullable
    private final BillingListener billingListener;
    private final boolean skipStaleRequests;
    private final boolean autoRecover;

    Configuration(@NonNull final Set<BillingProvider> providers,
                  @Nullable final BillingListener billingListener,
                  final boolean skipStaleRequests,
                  final boolean autoRecover) {
        this.skipStaleRequests = skipStaleRequests;
        this.autoRecover = autoRecover;
        this.providers = Collections.unmodifiableSet(providers);
        this.billingListener = billingListener;
    }

    /**
     * Gets supported billing providers.
     *
     * @return Collection of BillingProvider objects.
     */
    @SuppressWarnings("TypeMayBeWeakened")
    @NonNull
    public Set<BillingProvider> getProviders() {
        return providers;
    }

    /**
     * Gets persistent listener which is used to handle all billing events.
     *
     * @return BillingListener object. Can be null.
     */
    @Nullable
    public BillingListener getBillingListener() {
        return billingListener;
    }

    public boolean skipStaleRequests() {
        return skipStaleRequests;
    }

    /**
     * Indicates whether library should attempt to pick another suitable {@link BillingProvider} if
     * current one becomes unavailable.
     *
     * @return True if library will attempt to pick another BillingProvider. False otherwise.
     */
    public boolean autoRecover() {
        return autoRecover;
    }

    /**
     * Builder class for {@link Configuration} object.
     */
    public static class Builder {

        @NonNull
        private final Set<BillingProvider> providers = new LinkedHashSet<>();
        @Nullable
        private BillingListener billingListener;
        private boolean skipStaleRequests = true;
        private boolean autoRecover;

        /**
         * Adds supported billing provider.
         * <p>
         * During setup process billing providers will be considered in the order they were added.
         *
         * @param provider BillingProvider object to add.
         *
         * @return this object.
         */
        public Builder addBillingProvider(@NonNull final BillingProvider provider) {
            providers.add(provider);
            return this;
        }

        /**
         * Sets global listener to handle all billing events.
         * <p>
         * This listener will be stored in a static reference.
         *
         * @param billingListener BillingListener object to use.
         *
         * @return this object.
         *
         * @see DefaultBillingListener
         */
        public Builder setBillingListener(@Nullable final BillingListener billingListener) {
            this.billingListener = billingListener;
            return this;
        }

        public Builder setSkipStaleRequests(final boolean skipStaleRequests) {
            this.skipStaleRequests = skipStaleRequests;
            return this;
        }

        /**
         * Sets flag indicating whether library should attempt to substitute current
         * {@link BillingProvider} if it becomes unavailable.
         *
         * @param autoRecover True to attempt substitution of unavailable provider.
         *
         * @return this object.
         *
         * @see BillingProvider#isAvailable()
         */
        public Builder setAutoRecover(final boolean autoRecover) {
            this.autoRecover = autoRecover;
            return this;
        }

        /**
         * Constructs new Configuration object.
         *
         * @return Newly constructed Configuration instance.
         */
        public Configuration build() {
            return new Configuration(providers, billingListener, skipStaleRequests, autoRecover);
        }
    }
}
