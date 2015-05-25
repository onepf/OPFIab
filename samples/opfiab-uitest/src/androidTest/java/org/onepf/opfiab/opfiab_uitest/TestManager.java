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
import android.util.Log;
import android.util.Pair;
import org.onepf.opfiab.listener.BillingListener;
import org.onepf.opfiab.model.event.SetupResponse;
import org.onepf.opfiab.model.event.SetupStartedEvent;
import org.onepf.opfiab.model.event.billing.*;
import org.onepf.opfiab.opfiab_uitest.validators.EventValidator;

import java.util.*;

/**
 * @author antonpp
 * @since 15.05.15
 */
public class TestManager implements BillingListener {

    private static final Handler HANDLER = new Handler(Looper.getMainLooper());
    private static final String TAG = TestManager.class.getSimpleName();

    private final Queue<EventValidator> eventValidators;
    private final List<Pair<Object, Boolean>> receivedEvents;
    private final boolean skipWrongEvents;
    private final TestResultListener resultListener;

    private int currentEvent;
    private String errorMsg;
    private boolean isTestOver = false;

    private Runnable timeoutRunnable;

    private TestManager(Queue<EventValidator> eventValidators, boolean skipWrongEvents, TestResultListener resultListener) {
        this.eventValidators = eventValidators;
        this.skipWrongEvents = skipWrongEvents;
        this.resultListener = resultListener;
        this.receivedEvents = new ArrayList<>();
    }

    private void validateEvent(Object event) {
//        if (isTestOver) {
//            return;
//        }
        final EventValidator validator = eventValidators.peek();
        final boolean validationResult = validator.validate(event);
        if (validationResult) {
            eventValidators.poll();
        } else if (skipWrongEvents) {
            Log.d(TAG, "skipping event " + event.getClass().getSimpleName());
        } else {
            finishTest(false);
        }
        receivedEvents.add(new Pair<>(event, validationResult));
        if (eventValidators.isEmpty()) {
            finishTest(true);
        }
    }

    public void startTest(final long timeout) {
        HANDLER.postDelayed(timeoutRunnable = new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, String.format("Did not receive all expected events (%d of %d). %s", currentEvent, eventValidators.size(), getStringEvents()));
                finishTest(false);
            }
        }, timeout);
    }

    private void finishTest(boolean result) {
        HANDLER.removeCallbacks(timeoutRunnable);
        isTestOver = true;
        resultListener.onTestResult(result);
    }

    public List<Pair<Object, Boolean>> getReceivedEvents() {
        return receivedEvents;
    }

    private String getStringEvents() {
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

        private final Queue<EventValidator> eventValidators = new LinkedList<>();
        private boolean skipWrongEvents = true;
        private TestResultListener resultListener;

        public Builder setResultListener(TestResultListener resultListener) {
            this.resultListener = resultListener;
            return this;
        }

        public Builder addEvent(EventValidator eventValidator) {
            eventValidators.add(eventValidator);
            return this;
        }

        public Builder addEvents(EventValidator... eventValidators) {
            return addEvents(Arrays.asList(eventValidators));
        }

        public Builder addEvents(final Collection<EventValidator> eventValidators) {
            this.eventValidators.addAll(eventValidators);
            return this;
        }

        public Builder setSkipWrongEvents(boolean skipWrongEvents) {
            this.skipWrongEvents = skipWrongEvents;
            return this;
        }

        public TestManager build() {
            if (resultListener == null) {
                throw new IllegalStateException("ResultListener must be set.");
            }
            return new TestManager(eventValidators, skipWrongEvents, resultListener);
        }
    }
}
