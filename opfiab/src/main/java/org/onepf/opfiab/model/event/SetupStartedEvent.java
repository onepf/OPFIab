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

import org.onepf.opfiab.OPFIab;
import org.onepf.opfiab.model.Configuration;

/**
 * Class intended to indicate that setup process was started.
 *
 * @see OPFIab#setup()
 */
public class SetupStartedEvent {

    @NonNull
    private final Configuration configuration;

    public SetupStartedEvent(@NonNull final Configuration configuration) {
        this.configuration = configuration;
    }

    /**
     * Gets configuration used for the setup.
     *
     * @return Configuration object.
     */
    @NonNull
    public Configuration getConfiguration() {
        return configuration;
    }
}
