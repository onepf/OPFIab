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

package org.onepf.opfiab.opfiab_uitest.manager;

import java.util.Collection;
import java.util.HashSet;

/**
 * @author antonpp
 * @since 29.05.15
 */
public abstract class TestManagerAdapter {

    private final Collection<TestManager> testManagers = new HashSet<>();

    public TestManagerAdapter(TestManager testManager) {
        testManagers.add(testManager);
    }

    public TestManagerAdapter() {
        // TestManager should be added later
    }

    public void validateEvent(Object event) {
        for (TestManager testManager: testManagers) {
            testManager.validateEvent(event);
        }
    }

    public void addTestManager(TestManager testManager) {
        testManagers.add(testManager);
    }

    public void removeTestManager(TestManager testManager) {
        testManagers.remove(testManager);
    }
}
