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

package org.onepf.opfiab.opfiab_uitest.util.validators;

import android.support.annotation.Nullable;

import org.onepf.opfiab.model.billing.Purchase;
import org.onepf.opfiab.model.event.billing.InventoryResponse;
import org.onepf.opfiab.verification.VerificationResult;
import org.onepf.opfutils.OPFLog;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author antonpp
 * @since 01.06.15
 */
public class InventoryResponseValidator extends TypedEventValidator<InventoryResponse> {

    private final String name;
    private final boolean isSuccessful;
    @Nullable
    private final Map<Purchase, VerificationResult> expectedInventory;

    public InventoryResponseValidator(final String name, final boolean isSuccessful) {
        this(name, isSuccessful, null);
    }

    public InventoryResponseValidator(final String name, final boolean isSuccessful,
                                      @Nullable final Map<Purchase, VerificationResult> expectedInventory) {
        super(InventoryResponse.class);
        this.name = name;
        this.isSuccessful = isSuccessful;
        if (expectedInventory != null) {
            this.expectedInventory = new HashMap<>(expectedInventory);
        } else {
            this.expectedInventory = null;
        }
    }

    @SuppressWarnings("PMD.")
    @Override
    public boolean validate(final Object event, final boolean isLogging, final String logTag) {
        if (!super.validate(event, isLogging, logTag)) {
            return false;
        }
        final InventoryResponse response = (InventoryResponse) event;

        if (expectedInventory != null && validateInventory(response, isLogging, logTag)) {
            return false;
        }

        final boolean result;
        final String msg;

        if (response.isSuccessful() != isSuccessful) {
            msg = "Not expected success result";
            result = false;
        } else if (response.getProviderName() == null) {
            msg = "Provider info is set to null";
            result = false;
        } else if (response.getProviderName().equals(name)) {
            msg = "";
            result = true;
        } else {
            msg = "Wrong provider's name";
            result = false;
        }
        if (isLogging && !msg.isEmpty()) {
            OPFLog.e(String.format("[%s]: %s", logTag, msg));
        }
        return result;
    }

    private boolean validateInventory(InventoryResponse response, boolean isLogging,
                                      String logTag) {
        if (expectedInventory == null) {
            throw new IllegalStateException();
        }
        final Map<Purchase, VerificationResult> receivedInventory = response.getInventory();
        if (receivedInventory == null) {
            if (isLogging) {
                OPFLog.e(String.format("[%s]: %s", logTag, "received empty inventory"));
            }
            return false;
        }
        final Set<Map.Entry<Purchase, VerificationResult>> received = receivedInventory.entrySet();
        final Set<Map.Entry<Purchase, VerificationResult>> expected = expectedInventory.entrySet();

        if (received.containsAll(expected) && expected.containsAll(received)) {
            return true;
        } else {
            if (isLogging) {
                OPFLog.e(String.format("[%s]: %s", logTag,
                                       "Received inventory differs with the expected one"));
            }
            return false;
        }
    }
}
