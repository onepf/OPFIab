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

package org.onepf.opfiab.util;

import android.support.annotation.Nullable;

import org.onepf.opfutils.OPFChecks;
import org.onepf.opfutils.OPFLog;
import org.onepf.opfutils.OPFUtils;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class SyncedReference<E> {

    private final CountDownLatch latch = new CountDownLatch(1);
    @Nullable
    private volatile E model;

    public void set(@Nullable final E model) {
        if (latch.getCount() == 0L) {
            throw new IllegalStateException();
        }
        this.model = model;
        latch.countDown();
    }

    @Nullable
    public E get(final long timeout) {
        OPFChecks.checkThread(false);
        try {
            if (latch.await(timeout, TimeUnit.MILLISECONDS)) {
                return model;
            }
        } catch (InterruptedException exception) {
            OPFLog.e("", exception);
        }
        return null;
    }

    @Nullable
    public E get() {
        return model;
    }
}
