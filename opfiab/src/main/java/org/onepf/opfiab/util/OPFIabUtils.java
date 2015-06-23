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

package org.onepf.opfiab.util;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.json.JSONException;
import org.onepf.opfiab.ActivityMonitor;
import org.onepf.opfiab.model.JsonCompatible;
import org.onepf.opfiab.model.event.billing.BillingRequest;
import org.onepf.opfutils.OPFLog;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.Reference;
import java.util.Collection;
import java.util.Iterator;

import static android.content.pm.PackageManager.GET_SIGNATURES;


/**
 * Collection of handy utility methods.
 * <p/>
 * Intended for internal use.
 */
public final class OPFIabUtils {

    private static final int JSON_SPACES = 4;


    private OPFIabUtils() {
        throw new UnsupportedOperationException();
    }


    /**
     * Converts supplied object to human-readable JSON representation.
     *
     * @param jsonCompatible Object to convert.
     *
     * @return Human-readable string, can't be null.
     */
    @NonNull
    public static String toString(@NonNull final JsonCompatible jsonCompatible) {
        try {
            return jsonCompatible.toJson().toString(JSON_SPACES);
        } catch (JSONException exception) {
            OPFLog.e("", exception);
        }
        return "";
    }

    @NonNull
    public static String toString(@NonNull final InputStream inputStream) {
        final BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        final StringBuilder builder = new StringBuilder();
        try {
            for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                builder.append(line);
            }
            return builder.toString();
        } catch (IOException exception) {
            OPFLog.e("", exception);
        }
        return "";
    }

    /**
     * Removes first element from supplied collection.
     *
     * @param collection Collection to remove element from.
     *
     * @return Removed object or null.
     */
    @Nullable
    public static <E> E poll(@NonNull final Collection<E> collection) {
        if (collection.isEmpty()) {
            return null;
        }
        final Iterator<E> iterator = collection.iterator();
        final E e = iterator.next();
        iterator.remove();
        return e;
    }

    /**
     * Retrieves signature form supplied package.
     *
     * @param context     Context object to get {@link PackageManager} from.
     * @param packageName Package to retrieve signature for.
     *
     * @return Signature object if package found, null otherwise.
     */
    @SuppressWarnings("PackageManagerGetSignatures")
    @NonNull
    public static Signature[] getPackageSignatures(@NonNull final Context context,
                                                   @NonNull final String packageName) {
        final PackageManager packageManager = context.getPackageManager();
        try {
            final PackageInfo info = packageManager.getPackageInfo(packageName, GET_SIGNATURES);
            final Signature[] signatures = info.signatures;
            if (signatures != null) {
                return signatures;
            }
        } catch (PackageManager.NameNotFoundException exception) {
            OPFLog.e("", exception);
        }
        return new Signature[0];
    }

    public static boolean isStale(@NonNull final BillingRequest request) {
        final Reference<Activity> reference = request.getActivity();
        final Activity activity = reference == null ? null : reference.get();
        return reference != null && (activity == null || !ActivityMonitor.isStarted(activity));
    }

}
