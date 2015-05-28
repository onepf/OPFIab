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

package org.onepf.opfiab.opfiab_uitest;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.util.Pair;

import org.onepf.opfiab.listener.BillingListener;
import org.onepf.opfiab.model.event.SetupResponse;
import org.onepf.opfiab.model.event.SetupStartedEvent;
import org.onepf.opfiab.model.event.billing.BillingRequest;
import org.onepf.opfiab.model.event.billing.BillingResponse;
import org.onepf.opfiab.model.event.billing.ConsumeResponse;
import org.onepf.opfiab.model.event.billing.InventoryResponse;
import org.onepf.opfiab.model.event.billing.PurchaseResponse;
import org.onepf.opfiab.model.event.billing.SkuDetailsResponse;
import org.onepf.opfiab.opfiab_uitest.validators.EventValidator;
import org.onepf.opfutils.OPFLog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @author antonpp
 * @since 15.05.15
 */
public class TestManager implements BillingListener {

    private static final Handler HANDLER = new Handler(Looper.getMainLooper());

    private final Queue<EventValidator> eventValidators;
    private final List<Pair<Object, Boolean>> receivedEvents;
    private final boolean skipWrongEvents;
    private final Strategy strategy;

    private int currentEvent;
    private String errorMsg;
    private boolean isTestOver = false;

    private Runnable timeoutRunnable;
    private CountDownLatch testLatch;
    private boolean testResult;

    private TestManager(Collection<EventValidator> eventValidators, boolean skipWrongEvents,
                        final Strategy strategy) {
        this.eventValidators = new LinkedList<>(eventValidators);
        this.skipWrongEvents = skipWrongEvents;
        this.strategy = strategy;
        this.receivedEvents = new ArrayList<>();
    }

    private synchronized void validateEvent(Object event) {
        if (isTestOver) {
            return;
        }
        final boolean validationResult;
        switch (strategy) {
            case ORDERED_EVENTS:
                validationResult = orderedValidate(event);
                break;
            case UNORDERED_EVENTS:
                validationResult = unorderedValidate(event);
                break;
            default:
                throw new IllegalArgumentException();
        }
        receivedEvents.add(new Pair<>(event, validationResult));
        if (eventValidators.isEmpty()) {
            finishTest(true);
        }
    }

    private boolean unorderedValidate(Object event) {
        EventValidator matchedValidator = null;
        for (EventValidator eventValidator : eventValidators) {
            if (eventValidator.validate(event, false)) {
                matchedValidator = eventValidator;
                break;
            }
        }
        final boolean validationResult = (matchedValidator != null);
        if (validationResult) {
            eventValidators.remove(matchedValidator);
        } else if (skipWrongEvents) {
            OPFLog.e("skipping event " + event.getClass().getSimpleName());
        } else {
            finishTest(false);
        }
        return validationResult;
    }

    private boolean orderedValidate(Object event) {
        final EventValidator validator = eventValidators.peek();
        final boolean validationResult = validator.validate(event, true);
        if (validationResult) {
            eventValidators.poll();
        } else if (skipWrongEvents) {
            OPFLog.e("skipping event " + event.getClass().getSimpleName());
        } else {
            finishTest(false);
        }
        return validationResult;
    }

    public void startTest() {
        isTestOver = false;
        testResult = false;
        testLatch = new CountDownLatch(1);
    }

    public boolean await(final long timeout) throws InterruptedException {
        if (testLatch == null || isTestOver) {
            startTest();
        }
        final boolean isTimeOver = !testLatch.await(timeout, TimeUnit.MILLISECONDS);
        if (isTimeOver) {
            OPFLog.e(String.format("Did not receive all expected events (%d not received). %s",
                                   eventValidators.size(), getStringEvents()));
            finishTest(false);
        }
        return testResult;
    }

    private synchronized void finishTest(boolean result) {
        isTestOver = true;
        testResult = result;
        testLatch.countDown();
    }

    public List<Pair<Object, Boolean>> getReceivedEvents() {
        return receivedEvents;
    }

    private synchronized String getStringEvents() {
        final StringBuilder sb = new StringBuilder("Received Events: [");
        if (receivedEvents.isEmpty()) {
            sb.append(']');
            return sb.toString();
        }
        for (Pair<Object, Boolean> event : receivedEvents) {
            sb.append(event.first.getClass().getSimpleName())
                    .append(String.format(" (%s)", event.second ? "+" : "-"))
                    .append(", \n");
        }
        sb.delete(sb.length() - 3, sb.length());
        sb.append("]");
        return sb.toString();
    }

    @Override
    public void onRequest(@NonNull BillingRequest billingRequest) {
        validateEvent(billingRequest);
    }

    @Override
    public void onResponse(@NonNull BillingResponse billingResponse) {
        validateEvent(billingResponse);
    }

    @Override
    public void onConsume(@NonNull ConsumeResponse consumeResponse) {
        validateEvent(consumeResponse);
    }

    @Override
    public void onInventory(@NonNull InventoryResponse inventoryResponse) {
        validateEvent(inventoryResponse);
    }

    @Override
    public void onPurchase(@NonNull PurchaseResponse purchaseResponse) {
        validateEvent(purchaseResponse);
    }

    @Override
    public void onSetupStarted(@NonNull SetupStartedEvent setupStartedEvent) {
        validateEvent(setupStartedEvent);
    }

    @Override
    public void onSetupResponse(@NonNull SetupResponse setupResponse) {
        validateEvent(setupResponse);
    }

    @Override
    public void onSkuDetails(@NonNull SkuDetailsResponse skuDetailsResponse) {
        validateEvent(skuDetailsResponse);
    }

    public interface TestResultListener {
        void onTestResult(boolean passed);
    }

    public final static class Builder {

        private final Collection<EventValidator> eventValidators = new ArrayList<>();
        private boolean skipWrongEvents = true;
        private Strategy strategy = Strategy.ORDERED_EVENTS;

        public Builder setStrategy(Strategy strategy) {
            this.strategy = strategy;
            return this;
        }

        public Builder expectEvent(EventValidator eventValidator) {
            eventValidators.add(eventValidator);
            return this;
        }

        public Builder expectEvents(EventValidator... eventValidators) {
            return expectEvents(Arrays.asList(eventValidators));
        }

        public Builder expectEvents(final Collection<EventValidator> eventValidators) {
            this.eventValidators.addAll(eventValidators);
            return this;
        }

        public Builder setSkipWrongEvents(boolean skipWrongEvents) {
            this.skipWrongEvents = skipWrongEvents;
            return this;
        }

        public TestManager build() {
            return new TestManager(eventValidators, skipWrongEvents, strategy);
        }
    }

    public enum Strategy {
        ORDERED_EVENTS, UNORDERED_EVENTS
    }
}
