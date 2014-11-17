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

package org.onepf.opfiab.util;

public final class OPFIabUtils {

    private OPFIabUtils() {
        throw new UnsupportedOperationException();
    }


    private static final String packageName = OPFIabUtils.class.getPackage().getName();

    private static final String EXTRA_ACTION = packageName + "action";
    private static final String EXTRA_RESPONSE_STATUS = packageName + "responseStatus";
    private static final String EXTRA_SETUP_STATUS = packageName + "setupStatus";
    private static final String EXTRA_INVENTORY = packageName + "inventory";
    private static final String EXTRA_PURCHASE = packageName + "purchase";
    private static final String EXTRA_SKU_INFOS = packageName + "";


}
