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

package org.onepf.opfiab.opfiab_uitest.validators;

import org.onepf.opfiab.model.event.billing.PurchaseRequest;
import org.onepf.opfutils.OPFLog;

/**
 * @author antonpp
 * @since 28.05.15
 */
public class PurchaseRequestValidator extends TypedEventValidator<PurchaseRequest> {

    private final String sku;

    public PurchaseRequestValidator(String sku) {
        super(PurchaseRequest.class);
        this.sku = sku;
    }

    @Override
    public boolean validate(Object event, final boolean isLogging, final String logTag) {
        if (!super.validate(event, isLogging, logTag)) {
            return false;
        }
        final PurchaseRequest request = (PurchaseRequest) event;
        if (request.getSku().equals(sku)) {
            return true;
        } else {
            if (isLogging) {
                OPFLog.e(String.format("[%s]: %s", logTag, "Wrong provider's sku"));
            }
            return false;
        }
    }
}
