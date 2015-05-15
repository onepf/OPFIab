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

package org.onepf.opfiab.opfiab_testmanager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * @author antonpp
 * @since 15.05.15
 */
public class TestManager {

    private final List<ActionValidator> actionValidators;

    private TestManager(List<ActionValidator> actionValidators) {
        this.actionValidators = actionValidators;
    }

    public final static class Builder {

        private final List<ActionValidator> actionValidators = new ArrayList<>();

        public Builder addAction(ActionValidator actionValidator) {
            actionValidators.add(actionValidator);
            return this;
        }

        public Builder addActions(ActionValidator... actionValidators) {
            return addActions(Arrays.asList(actionValidators));
        }

        public Builder addActions(final Collection<ActionValidator> actionValidators) {
            this.actionValidators.addAll(actionValidators);
            return this;
        }

        public TestManager build() {
            return new TestManager(actionValidators);
        }
    }
}
