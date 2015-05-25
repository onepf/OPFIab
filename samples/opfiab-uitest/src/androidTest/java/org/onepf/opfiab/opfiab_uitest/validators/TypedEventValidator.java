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

package org.onepf.opfiab.opfiab_uitest.validators;

import android.util.Log;

/**
 * @author antonpp
 * @since 25.05.15
 */
public abstract class TypedEventValidator<T> implements EventValidator {


    private static final String TAG = TypedEventValidator.class.getSimpleName();
    protected final Class<T> clazz;

    protected TypedEventValidator(Class<T> clazz) {
        this.clazz = clazz;
    }

    @Override
    public boolean validate(Object event) {
        final boolean result = event.getClass().equals(clazz);
        if (!result) {
            Log.d(TAG, String.format("Wrong Type Event. Expected: %s. Received: %s", clazz.getSimpleName(), event.getClass().getSimpleName()));
        }
        return result;
    }
}
