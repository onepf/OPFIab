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

package org.onepf.opfiab.model.event;

import android.app.Application;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;
import org.onepf.opfiab.OPFIab;
import org.onepf.opfiab.billing.BillingProvider;
import org.onepf.opfiab.model.Configuration;
import org.onepf.opfiab.model.JsonCompatible;
import org.onepf.opfiab.util.OPFIabUtils;
import org.onepf.opfutils.OPFLog;

import java.util.Arrays;
import java.util.Collection;

import static org.json.JSONObject.NULL;
import static org.onepf.opfiab.model.event.SetupResponse.Status.PROVIDER_CHANGED;
import static org.onepf.opfiab.model.event.SetupResponse.Status.SUCCESS;


/**
 * Class intended to indicate that setup process has finished.
 *
 * @see SetupStartedEvent
 * @see OPFIab#setup()
 */
public class SetupResponse implements JsonCompatible {

    private static final String NAME_STATUS = "status";
    private static final String NAME_PROVIDER = "provider";
    private static final String NAME_AUTHORIZED = "authorized";

    /**
     * Status of corresponding {@link SetupResponse}.
     */
    public enum Status {

        /**
         * {@link BillingProvider} has been successfully picked.
         */
        SUCCESS,
        /**
         * Setup resulted in a different {@link BillingProvider} being picked then one that was used
         * for this application previously.
         * <br>
         * Some items might be missing form user's inventory.
         */
        PROVIDER_CHANGED,
        /**
         * Library failed to pick suitable {@link BillingProvider}.
         */
        FAILED,
    }

    private static final Collection<Status> SUCCESSFUL =
            Arrays.asList(SUCCESS, PROVIDER_CHANGED);


    @NonNull
    private final Configuration configuration;
    @NonNull
    private final Status status;
    @Nullable
    private final BillingProvider billingProvider;
    private final boolean authorized;

    public SetupResponse(final @NonNull Configuration configuration,
                         @NonNull final Status status,
                         @Nullable final BillingProvider billingProvider,
                         final boolean authorized) {
        this.configuration = configuration;
        this.status = status;
        this.billingProvider = billingProvider;
        this.authorized = authorized;
        if (billingProvider == null && isSuccessful()) {
            throw new IllegalArgumentException();
        }
    }

    public SetupResponse(final Configuration configuration,
                         @NonNull final Status status,
                         @Nullable final BillingProvider billingProvider) {
        this(configuration, status, billingProvider,
             billingProvider != null && billingProvider.isAuthorised());
    }

    /**
     * Gets configuration object which is used for the setup.
     *
     * @return Configuration object.
     * @see OPFIab#init(Application, Configuration)
     */
    @NonNull
    public Configuration getConfiguration() {
        return configuration;
    }

    /**
     * Gets status of this setup event.
     *
     * @return Status.
     */
    @NonNull
    public Status getStatus() {
        return status;
    }

    /**
     * Gets billing provider that was picked during setup.
     *
     * @return BillingProvider object if setup was successful, null otherwise.
     * @see #isSuccessful()
     */
    @Nullable
    public BillingProvider getBillingProvider() {
        return billingProvider;
    }

    /**
     * Indicates whether picked billing provider is authorised or not.
     *
     * @return True if BillingProvider was picked and does not require authorization, false
     * otherwise.
     * @see BillingProvider#isAuthorised()
     */
    public boolean isAuthorized() {
        return authorized;
    }

    /**
     * Indicates whether billing provider was successfully picked or not.
     *
     * @return True if BillingProvider was picked, false otherwise.
     */
    public final boolean isSuccessful() {
        return SUCCESSFUL.contains(status);
    }

    @NonNull
    @Override
    public JSONObject toJson() {
        final JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(NAME_STATUS, status);
            jsonObject.put(NAME_PROVIDER, billingProvider == null
                    ? NULL
                    : billingProvider.getInfo().toJson());
            jsonObject.put(NAME_AUTHORIZED, authorized);
        } catch (JSONException exception) {
            OPFLog.e("", exception);
        }
        return jsonObject;
    }

    @Override
    public String toString() {
        return OPFIabUtils.toString(this);
    }
}
