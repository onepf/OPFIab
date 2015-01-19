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

package org.onepf.opfiab.model.event;

import android.support.annotation.NonNull;

import java.util.Random;

public class BillingEvent {

    private static final Random RANDOM = new Random();

    public static enum Type {
        SETUP,

        CONSUME,
        PURCHASE,
        SKU_DETAILS,
        INVENTORY,
    }


    private final int id = RANDOM.nextInt();

    @NonNull
    private final Type type;

    protected BillingEvent(@NonNull final Type type) {
        this.type = type;
    }

    public int getId() {
        return id;
    }

    @NonNull
    public Type getType() {
        return type;
    }

    //CHECKSTYLE:OFF
    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof BillingEvent)) return false;

        final BillingEvent that = (BillingEvent) o;

        if (id != that.id) return false;
        //noinspection RedundantIfStatement
        if (type != that.type) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + type.hashCode();
        return result;
    }
    //CHECKSTYLE:ON
}
