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

import org.onepf.opfiab.model.event.billing.ConsumeResponse;
import org.onepf.opfutils.OPFLog;

/**
 * @author antonpp
 * @since 01.06.15
 */
public class ConsumeResponseValidator extends TypedEventValidator<ConsumeResponse> {

    private final String name;
    private final boolean isSuccessful;

    public ConsumeResponseValidator(final String name, final boolean isSuccessful) {
        super(ConsumeResponse.class);
        this.name = name;
        this.isSuccessful = isSuccessful;
    }

    @Override
    public boolean validate(final Object event, final boolean isLogging, final String logTag) {
        if (!super.validate(event, isLogging, logTag)) {
            return false;
        }
        final ConsumeResponse response = (ConsumeResponse) event;
        final boolean result;
        final String msg;
        if (response.isSuccessful() != isSuccessful) {
            msg = "Not expected success result";
            result = false;
        } else if (response.getProviderInfo() == null) {
            msg = "Provider info is set to null";
            result = false;
        } else if (response.getProviderInfo().getName().equals(name)) {
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
}
