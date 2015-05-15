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

package org.onepf.opfiab.opfiab_testmanager;

import android.support.annotation.NonNull;

import org.onepf.opfiab.listener.BillingListener;
import org.onepf.opfiab.model.event.SetupResponse;
import org.onepf.opfiab.model.event.SetupStartedEvent;
import org.onepf.opfiab.model.event.billing.BillingRequest;
import org.onepf.opfiab.model.event.billing.BillingResponse;
import org.onepf.opfiab.model.event.billing.ConsumeResponse;
import org.onepf.opfiab.model.event.billing.InventoryResponse;
import org.onepf.opfiab.model.event.billing.PurchaseResponse;
import org.onepf.opfiab.model.event.billing.SkuDetailsResponse;
import org.onepf.opfiab.opfiab_testmanager.validators.ActionValidator;
import org.onepf.opfiab.opfiab_testmanager.validators.ActionValidator.ValidationResult;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @author antonpp
 * @since 15.05.15
 */
public class TestManager implements BillingListener {

    private final List<ActionValidator> actionValidators;
    private final CountDownLatch testLatch;

    private int currentAction;
    private String errorMsg;

    private TestManager(List<ActionValidator> actionValidators) {
        this.actionValidators = actionValidators;
        this.testLatch = new CountDownLatch(actionValidators.size());
    }

    private void validateAction(Object action) {
        final ActionValidator validator = actionValidators.get(currentAction);
        if (!action.getClass().equals(validator.getActionClass())) {
            fail(String.format("Invalid action received. Expected: %s, received: %s.",
                    validator.getActionClass(), action.getClass()));
        }
        final ValidationResult validationResult = validator.validate(validator.getActionClass().cast(action));
        if (validationResult != ValidationResult.OK) {
            fail(String.format("Validation failed on %d action. Action:\n%s", currentAction, action));
        }
        testLatch.countDown();
        ++currentAction;
    }

    public boolean startTest(long timeout) throws InterruptedException {
        return (errorMsg == null) && testLatch.await(timeout, TimeUnit.MILLISECONDS);
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    private void fail(String msg) {
        errorMsg = msg;
        while (testLatch.getCount() > 0) {
            testLatch.countDown();
        }
    }

    @Override
    public void onRequest(@NonNull BillingRequest billingRequest) {
        validateAction(billingRequest);
    }

    @Override
    public void onResponse(@NonNull BillingResponse billingResponse) {
        validateAction(billingResponse);
    }

    @Override
    public void onConsume(@NonNull ConsumeResponse consumeResponse) {
        validateAction(consumeResponse);
    }

    @Override
    public void onInventory(@NonNull InventoryResponse inventoryResponse) {
        validateAction(inventoryResponse);
    }

    @Override
    public void onPurchase(@NonNull PurchaseResponse purchaseResponse) {
        validateAction(purchaseResponse);
    }

    @Override
    public void onSetupStarted(@NonNull SetupStartedEvent setupStartedEvent) {
        validateAction(setupStartedEvent);
    }

    @Override
    public void onSetupResponse(@NonNull SetupResponse setupResponse) {
        validateAction(setupResponse);
    }

    @Override
    public void onSkuDetails(@NonNull SkuDetailsResponse skuDetailsResponse) {
        validateAction(skuDetailsResponse);
    }

    public final static class Builder {

        private final List<ActionValidator> actionValidators = new ArrayList<>();

        public Builder addAction(ActionValidator actionValidator) {
            actionValidators.add(actionValidator);
            return this;
        }

        public Builder addActions(ActionValidator... actionValidators) {
            return addActions(Arrays.asList(actionValidators));
        }

        public Builder addActions(final Collection<ActionValidator> actionValidators) {
            this.actionValidators.addAll(actionValidators);
            return this;
        }

        public TestManager build() {
            return new TestManager(actionValidators);
        }
    }
}
