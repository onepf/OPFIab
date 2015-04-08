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

package org.onepf.sample.trivialdrive;

import android.content.Context;
import android.content.SharedPreferences;

public final class TrivialData {

    private static final String NAME = "trivialdrive";

    private static final int GAS_INITIAL = 2;
    private static final int GAS_MAX = 4;

    private static final String KEY_FIRST_LAUNCH = "first_launch";
    private static final String KEY_GAS = "gas";


    private static SharedPreferences preferences;

    public static void init(final Context context) {
        preferences = context.getSharedPreferences(NAME, Context.MODE_PRIVATE);
        if (preferences.getBoolean(KEY_FIRST_LAUNCH, true)) {
            preferences.edit()
                    .putBoolean(KEY_FIRST_LAUNCH, false)
                    .putInt(KEY_GAS, GAS_INITIAL)
                    .apply();
        }
    }

    public static int getGas() {
        return preferences.getInt(KEY_GAS, 0);
    }

    public static void addGas() {
        if (!canAddGas()) {
            throw new IllegalStateException();
        }
        preferences.edit().putInt(KEY_GAS, getGas() + 1).apply();
    }

    public static void spendGas() {
        final int gas = getGas();
        if (gas <= 0) {
            throw new IllegalStateException();
        }
        preferences.edit().putInt(KEY_GAS, gas - 1).apply();
    }

    public static boolean canAddGas() {
        return getGas() < GAS_MAX;
    }

    public static boolean canSpendGas() {
        return getGas() > 0;
    }

    public static void registerOnSharedPreferenceChangeListener(
            final SharedPreferences.OnSharedPreferenceChangeListener listener) {
        preferences.registerOnSharedPreferenceChangeListener(listener);
    }

    public static void unregisterOnSharedPreferenceChangeListener(
            final SharedPreferences.OnSharedPreferenceChangeListener listener) {
        preferences.unregisterOnSharedPreferenceChangeListener(listener);
    }


    private TrivialData() {
        throw new UnsupportedOperationException();
    }
}
