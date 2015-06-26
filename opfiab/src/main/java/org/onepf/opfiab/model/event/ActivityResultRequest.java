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

package org.onepf.opfiab.model.event;

import android.support.annotation.NonNull;

import org.onepf.opfiab.model.event.android.ActivityResult;
import org.onepf.opfiab.model.event.billing.BillingRequest;
import org.onepf.opfiab.util.ActivityForResultLauncher;
import org.onepf.opfiab.util.SyncedReference;

public final class ActivityResultRequest {

    @NonNull
    private final BillingRequest request;
    @NonNull
    private final ActivityForResultLauncher launcher;
    @NonNull
    private final SyncedReference<ActivityResult> syncActivityResult;

    public ActivityResultRequest(@NonNull final BillingRequest request,
                                 @NonNull final ActivityForResultLauncher launcher,
                                 @NonNull final SyncedReference<ActivityResult> syncActivityResult) {
        this.request = request;
        this.launcher = launcher;
        this.syncActivityResult = syncActivityResult;
    }

    @NonNull
    public BillingRequest getRequest() {
        return request;
    }

    @NonNull
    public ActivityForResultLauncher getLauncher() {
        return launcher;
    }

    @NonNull
    public SyncedReference<ActivityResult> getSyncActivityResult() {
        return syncActivityResult;
    }
}
