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

public final class TrivialUtils {

    private static final String NAME = "trivialdrive";

    public static final int MAX_GAS = 4;

    public static final String KEY_SAMPLE = "sample";

    public static final String KEY_GAS = "gas";
    public static final String KEY_PREMIUM = "premium";

    public static final String SKU_GAS = "sku_gas";
    public static final String SKU_PREMIUM = "sku_premium";
    public static final String SKU_SUBSCRIPTION = "sku_subscription";

    public static final String AMAZON_SKU_GAS = "org.onepf.sample.trivialdrive.sku_gas";

    public static final String GOOGLE_SKU_GAS = "android.test.purchased";
    @SuppressWarnings("SpellCheckingInspection")
    public static final String GOOGLE_PLAY_KEY
            = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA5F8fASyrDFdaXrkoW8kNtwH5JIkLnNuTD5uE1a37TbI5LDZR" +
            "VgvMIYAtZ9CAHAfLnJ6OEZt0lvLLJSKVuS47VqYVhGZciOkX8TEihONBRwis6i9A3JnKfyqm0iiT+P0CEktOLuFLROIo13" +
            "utCIO++6h7A7/WLfxNV+Jnxfs9OEHyyPS+MdHxa0wtZGeAGiaN65BymsBQo7J/ABt2DFyMJP1R/nJM45F8yu4D6wSkUNKz" +
            "s/QbPfvHJQzq56/B/hbx59EkzkInqC567hrlUlX4bU5IvOTF/B1G+UMuKg80m3I1IcQk4FD2D9oJ3E+8IXG/1UdejrOsmq" +
            "DAzE7LkMl8xwIDAQAB";

    public static SharedPreferences getPreferences(final Context context) {
        return context.getSharedPreferences(NAME, Context.MODE_PRIVATE);
    }

    public static SampleType getSampleType(final Context context) {
        final SharedPreferences preferences = getPreferences(context);
        return SampleType.values()[preferences.getInt(KEY_SAMPLE, 0)];
    }

    public static void setSampleType(final Context context, final SampleType sampleType) {
        final SharedPreferences preferences = getPreferences(context);
        preferences.edit().putInt(KEY_SAMPLE, sampleType.ordinal()).apply();
    }

    public static int getGas(final Context context) {
        return getPreferences(context).getInt(KEY_GAS, 0);
    }

    public static void setGas(final Context context, final int gas) {
        getPreferences(context).edit().putInt(KEY_GAS, gas);
    }

    public static boolean canAddGas(final Context context) {
        return getGas(context) < MAX_GAS;
    }

    private TrivialUtils() {
        throw new UnsupportedOperationException();
    }
}
