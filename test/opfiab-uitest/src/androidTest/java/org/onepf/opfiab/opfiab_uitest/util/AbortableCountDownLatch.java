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

package org.onepf.opfiab.opfiab_uitest.util;

import android.support.annotation.NonNull;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @author antonpp
 * @since 18.05.15
 */
public class AbortableCountDownLatch extends CountDownLatch {

    protected boolean aborted = false;
    protected String abortMessage;

    /**
     * Constructs a {@code CountDownLatch} initialized with the given count.
     *
     * @param count the number of times {@link #countDown} must be invoked
     *              before threads can pass through {@link #await}
     * @throws IllegalArgumentException if {@code count} is negative
     */
    public AbortableCountDownLatch(int count) {
        super(count);
    }

    /**
     * Unblocks all threads waiting on this latch and cause them to receive an
     * AbortedException.  If the latch has already counted all the way down,
     * this method does nothing.
     */
    public void abort() {
        abort(null);
    }

    public void abort(String msg) {
        if (getCount() == 0) {
            return;
        }
        this.abortMessage = msg;
        this.aborted = true;
        while (getCount() > 0) {
            countDown();
        }
    }


    @Override
    public boolean await(long timeout, @NonNull TimeUnit unit) throws InterruptedException {
        final boolean result = super.await(timeout, unit);
        if (aborted) {
            throw new AbortedException(abortMessage);
        }
        return result;
    }

    @Override
    public void await() throws InterruptedException {
        super.await();
        if (aborted) {
            throw new AbortedException(abortMessage);
        }
    }


    public static class AbortedException extends InterruptedException {
        public AbortedException() {
        }

        public AbortedException(String detailMessage) {
            super(detailMessage);
        }
    }
}
