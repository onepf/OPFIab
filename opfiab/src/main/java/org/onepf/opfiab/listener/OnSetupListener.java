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

package org.onepf.opfiab.listener;

import android.support.annotation.NonNull;

import org.onepf.opfiab.model.event.SetupResponse;
import org.onepf.opfiab.model.event.SetupStartedEvent;

/**
 * Listener for library setup event.
 */
public interface OnSetupListener {

    /**
     * Called when library is being set up.
     *
     * @param setupStartedEvent Setup event sent by library.
     */
    void onSetupStarted(@NonNull final SetupStartedEvent setupStartedEvent);

    /**
     * Called when library setup is finished.
     *
     * @param setupResponse Setup event sent by library.
     */
    void onSetupResponse(@NonNull final SetupResponse setupResponse);
}
