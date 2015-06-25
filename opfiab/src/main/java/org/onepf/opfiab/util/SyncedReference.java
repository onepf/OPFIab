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

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class SyncedReference<E> {

    private final CountDownLatch latch = new CountDownLatch(1);
    private boolean isSet;

    @Nullable
    private volatile E model;

    @SuppressWarnings("PMD.AvoidSynchronizedAtMethodLevel")
    public void set(@Nullable final E model) {
        if (isSet) {
            OPFLog.logMethod(model);
            OPFLog.e("Attempt to re-set SyncedReference value.");
            return;
        }
        this.isSet = true;
        this.model = model;
        latch.countDown();
    }

    @Nullable
    public E get(final long timeout) {
        OPFChecks.checkThread(false);
        try {
            latch.await(timeout, TimeUnit.MILLISECONDS);
        } catch (InterruptedException exception) {
            OPFLog.e("", exception);
        }
        return model;
    }

    @Nullable
    public E get() {
        OPFChecks.checkThread(false);
        try {
            latch.await();
        } catch (InterruptedException exception) {
            OPFLog.e("", exception);
        }
        return model;
    }

    @Nullable
    public E getNow() {
        return model;
    }

}
