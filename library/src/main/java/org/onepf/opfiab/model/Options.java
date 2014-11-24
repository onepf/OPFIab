/*
 * Copyright 2012-2014 One Platform Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.onepf.opfiab.model;

import android.support.annotation.Nullable;

import org.onepf.opfiab.listener.BillingListener;

public final class Options {

    @Nullable
    final BillingListener billingListener;

    public Options(final @Nullable BillingListener billingListener) {
        this.billingListener = billingListener;
    }

    @Nullable
    public BillingListener getBillingListener() {
        return billingListener;
    }

    public static class Builder {

        private BillingListener billingListener;

        public Builder setBillingListener(final BillingListener billingListener) {
            this.billingListener = billingListener;
            return this;
        }

        public Options createOptions() {
            return new Options(billingListener);
        }
    }
}
