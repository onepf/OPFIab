/*
 * Copyright 2012-2014 One Platform Foundation
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

package org.onepf.opfiab.verification;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public class VerificationResult {

    public static enum VerificationStatus {

        SUCCESS,
        FAILED,
        ERROR,
    }

    @NonNull
    private final VerificationStatus status;

    @Nullable
    private final Exception exception;

    public VerificationResult(@NonNull final VerificationStatus status,
                              @Nullable final Exception exception) {
        this.status = status;
        this.exception = exception;
    }

    public VerificationResult(@NonNull final VerificationStatus status) {
        this(status, null);
    }

    @NonNull
    public VerificationStatus getStatus() {
        return status;
    }

    @Nullable
    public Exception getException() {
        return exception;
    }
}
