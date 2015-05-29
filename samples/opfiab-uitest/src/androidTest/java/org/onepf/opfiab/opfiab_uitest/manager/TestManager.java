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

package org.onepf.opfiab.opfiab_uitest.manager;

import android.util.Pair;

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
public class TestManager {

    private final Queue<EventValidator> eventValidators;
    private final List<Pair<Object, Boolean>> receivedEvents = new ArrayList<>();
    private final boolean skipWrongEvents;
    private final Strategy strategy;
    private final CountDownLatch testLatch = new CountDownLatch(1);
    private int currentEvent;
    private String errorMsg;
    private volatile boolean testResult = false;

    private TestManager(Collection<EventValidator> eventValidators, boolean skipWrongEvents,
                        final Strategy strategy) {
        this.eventValidators = new LinkedList<>(eventValidators);
        this.skipWrongEvents = skipWrongEvents;
        this.strategy = strategy;
    }

    synchronized void validateEvent(Object event) {
        if (testLatch.getCount() == 0) {
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
        if (!validationResult) {
            if (skipWrongEvents) {
                OPFLog.e("skipping event " + event.getClass().getSimpleName());
            } else {
                finishTest(false);
            }
        }
        if (eventValidators.isEmpty()) {
            finishTest(true);
        }
    }

    private boolean orderedValidate(Object event) {
        final EventValidator validator = eventValidators.peek();
        final boolean validationResult = validator.validate(event, true);
        if (validationResult) {
            eventValidators.poll();
        }
        return validationResult;
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
        }
        return validationResult;
    }

    private synchronized void finishTest(boolean result) {
        testResult = result;
        testLatch.countDown();
    }

    public boolean await(final long timeout) throws InterruptedException {
        if (testLatch == null) {
            throw new IllegalStateException();
        }
        final boolean isTimeOver = !testLatch.await(timeout, TimeUnit.MILLISECONDS);
        if (isTimeOver) {
            OPFLog.e(String.format("Did not receive all expected events (%d not received). %s",
                                   eventValidators.size(), getStringEvents()));
            finishTest(false);
        }
        return testResult;
    }

    private synchronized String getStringEvents() {
        final StringBuilder sb = new StringBuilder("Received Events: [");
        for (Pair<Object, Boolean> event : receivedEvents) {
            sb.append(event.first.getClass().getSimpleName())
                    .append(String.format(" (%s)", event.second ? "+" : "-"))
                    .append(", \n");
        }
        if (!receivedEvents.isEmpty()) {
            sb.delete(sb.length() - 3, sb.length());
        }
        sb.append(']');
        return sb.toString();
    }

    public List<Pair<Object, Boolean>> getReceivedEvents() {
        return receivedEvents;
    }

    public enum Strategy {
        ORDERED_EVENTS, UNORDERED_EVENTS
    }

    public final static class Builder {

        private final Collection<EventValidator> eventValidators = new ArrayList<>();
        private boolean skipWrongEvents = true;
        private Strategy strategy = Strategy.ORDERED_EVENTS;
        private TestManagerAdapter adapter = null;

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

        public Builder setAdapter(TestManagerAdapter adapter) {
            this.adapter = adapter;
            return this;
        }

        public TestManager build() {
            final TestManager testManager = new TestManager(eventValidators, skipWrongEvents, strategy);
            if (adapter != null) {
                adapter.addTestManager(testManager);
            }
            return testManager;
        }
    }
}
