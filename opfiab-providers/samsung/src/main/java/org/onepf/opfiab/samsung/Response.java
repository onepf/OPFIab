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

package org.onepf.opfiab.samsung;

import android.support.annotation.Nullable;

@SuppressWarnings("MagicNumber")
public enum Response {

    ERROR_NONE(0),
    PAYMENT_IS_CANCELED(1),
    ERROR_INITIALIZATION(-1000),
    ERROR_NEED_APP_UPGRADE(-1001),
    ERROR_COMMON(-1002),
    ERROR_ALREADY_PURCHASED(-1003),
    ERROR_WHILE_RUNNING(-1004),
    ERROR_PRODUCT_DOES_NOT_EXIST(-1005),
    ERROR_CONFIRM_INBOX(-1006),
    ERROR_ITEM_GROUP_ID_DOES_NOT_EXIST(-1007),
    ERROR_NETWORK_NOT_AVAILABLE(-1008),
    ERROR_IOEXCEPTION_ERROR(-1009),
    ERROR_SOCKET_TIMEOUT(-1010),
    ERROR_CONNECT_TIMEOUT(-1011);

    @Nullable
    public static Response fromCode(final int code) {
        for (final Response response : values()) {
            if (response.code == code) {
                return response;
            }
        }
        return null;
    }


    private final int code;

    Response(final int code) {
        this.code = code;
    }
}
