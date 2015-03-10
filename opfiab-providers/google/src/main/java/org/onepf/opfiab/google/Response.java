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

package org.onepf.opfiab.google;

import android.support.annotation.Nullable;

enum Response {
    BILLING_RESPONSE_RESULT_OK(0),
    BILLING_RESPONSE_RESULT_USER_CANCELED(1),
    BILLING_RESPONSE_RESULT_BILLING_UNAVAILABLE(3),
    BILLING_RESPONSE_RESULT_ITEM_UNAVAILABLE(4),
    BILLING_RESPONSE_RESULT_DEVELOPER_ERROR(5),
    BILLING_RESPONSE_RESULT_ERROR(6),
    BILLING_RESPONSE_RESULT_ITEM_ALREADY_OWNED(7),
    BILLING_RESPONSE_RESULT_ITEM_NOT_OWNED(8),;

    @Nullable
    static Response fromCode(final int code) {
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
