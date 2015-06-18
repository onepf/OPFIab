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

package org.onepf.opfiab.opfiab_uitest.util.validators;

import org.onepf.opfutils.OPFLog;

/**
 * @author antonpp
 * @since 03.06.15
 */
public class AlwaysFailValidator implements EventValidator {
    private static final StopObject STOP_OBJECT = new StopObject();

    public static StopObject getStopObject() {
        return STOP_OBJECT;
    }

    @Override
    public boolean validate(final Object event, final boolean isLogging, final String logTag) {
        final boolean result = event.equals(STOP_OBJECT);
        if (isLogging && !result) {
            OPFLog.e(String.format("[%s]: Received %s when STOP_OBJECT was expected", logTag,
                                   event.getClass().getSimpleName()));
        }
        return result;
    }

    private static final class StopObject {
        public StopObject() {
            super();
        }
    }
}
