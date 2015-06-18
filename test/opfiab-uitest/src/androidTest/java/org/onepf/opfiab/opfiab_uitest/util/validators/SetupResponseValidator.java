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

import org.onepf.opfiab.model.event.SetupResponse;
import org.onepf.opfutils.OPFLog;

/**
 * @author antonpp
 * @since 15.05.15
 */
public class SetupResponseValidator extends TypedEventValidator<SetupResponse> {

    private final String name;

    public SetupResponseValidator(String name) {
        super(SetupResponse.class);
        this.name = name;
    }

    @Override
    public boolean validate(Object event, final boolean isLogging, final String logTag) {
        if (!super.validate(event, isLogging, logTag)) {
            return false;
        }
        final SetupResponse setupResponse = (SetupResponse) event;
        final boolean result;
        final String msg;
        if (setupResponse.getBillingProvider() == null) {
            msg = "Billing provider is set to null";
            result =  false;
        } else if (setupResponse.getBillingProvider().getName().equals(name)) {
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
