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

/**
 * @author antonpp
 * @since 09.06.15
 */
public final class Constants {

    public static final String TEST_APP_PKG = "org.onepf.opfiab.opfiab_uitest";
    public static final String TEST_PROVIDER_PACKAGE = "org.onepf.opfiab.uitest";
    public static final String TEST_PROVIDER_NAME = "TEST_PROVIDER_NAME";
    public static final String TEST_PROVIDER_NAME_FMT = "TEST_PROVIDER_NAME_%s";
    public static final String SKU_CONSUMABLE = "org.onepf.opfiab.consumable";
    public static final String SKU_NONCONSUMABLE = "org.onepf.opfiab.nonconsumable";
    public static final String SKU_SUBSCRIPTION = "org.onepf.opfiab.subscription";

    public static final long WAIT_LAUNCH_SCREEN = 5000L;
    public static final long WAIT_REOPEN_ACTIVITY = 500L;
    public static final long WAIT_BILLING_PROVIDER = 1000L;
    public static final long WAIT_PURCHASE = 2 * WAIT_BILLING_PROVIDER;
    public static final long WAIT_INIT = 2 * WAIT_BILLING_PROVIDER;
    public static final long WAIT_TEST_MANAGER = 2 * WAIT_BILLING_PROVIDER;

    private Constants() {
        throw new UnsupportedOperationException();
    }
}
