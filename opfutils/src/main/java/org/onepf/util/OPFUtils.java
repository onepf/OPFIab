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

package org.onepf.util;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

public final class OPFUtils {

    private static final String ITEM_DIVIDER = ", ";

    private static final Handler HANDLER = new Handler(Looper.getMainLooper());

    private OPFUtils() {
        throw new UnsupportedOperationException();
    }

    public static boolean isMainThread() {
        return Looper.getMainLooper().getThread() == Thread.currentThread();
    }

    public static void post(final Runnable runnable) {
        HANDLER.post(runnable);
    }

    public static boolean isNetworkConnected(@NonNull final Context context) {
        final ConnectivityManager cm = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo activeNetworkInfo = cm.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    /**
     * Get version code of current application.
     *
     * @return If find app - return it's version code, else {@link Integer#MIN_VALUE}.
     */
    public static int getAppVersion(@NonNull final Context context)
            throws PackageManager.NameNotFoundException {

        final PackageInfo packageInfo = context.getPackageManager()
                .getPackageInfo(context.getPackageName(), 0);
        if (packageInfo == null) {
            throw new PackageManager.NameNotFoundException(context.getPackageName());
        }
        return packageInfo.versionCode;
    }

    /**
     * Check is application system.
     *
     * @param context    The current context.
     * @param appPackage Package of application for verify.
     * @return True when application is system, false - otherwise.
     */
    public static boolean isSystemApp(@NonNull final Context context,
                                      @NonNull final String appPackage) {
        try {
            final ApplicationInfo appInfo = context.getPackageManager().getApplicationInfo(
                    appPackage, 0);
            return (appInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0
                    || (appInfo.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0;
        } catch (PackageManager.NameNotFoundException ignore) {
            // ignore
        }
        return false;
    }

    /**
     * Check is application installed on device.
     *
     * @param context    The current context.
     * @param appPackage Package of application for verify.
     * @return True when application is installed, false - otherwise.
     */
    public static boolean isInstalled(@NonNull final Context context,
                                      @NonNull final String appPackage) {
        try {
            return context.getPackageManager().getApplicationInfo(appPackage, 0) != null;
        } catch (PackageManager.NameNotFoundException ignore) {
            // ignore
        }
        return false;
    }

    /**
     * Checks if the AndroidManifest contains a permission.
     *
     * @param permission The permission to test.
     * @return true if the permission is requested by the application.
     */
    public static boolean hasRequestedPermission(@NonNull final Context context,
                                                 @NonNull final String permission) {
        if (TextUtils.isEmpty(permission)) {
            throw new IllegalArgumentException("Permission can't be null or empty.");
        }

        try {
            final PackageInfo info = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), PackageManager.GET_PERMISSIONS);
            final String[] requestedPermissions = info.requestedPermissions;
            if (requestedPermissions != null) {
                for (String requestedPermission : requestedPermissions) {
                    if (TextUtils.equals(permission, requestedPermission)) {
                        return true;
                    }
                }
            }
        } catch (PackageManager.NameNotFoundException ignore) {
            // ignore
        }
        return false;
    }

    /**
     * Checks if an application with the passed name is the installer of the calling app.
     *
     * @param packageName The package name of the tested application.
     * @return true if the application with the passed package is the installer.
     */
    public static boolean isPackageInstaller(@NonNull final Context context,
                                             final String packageName) {
        final PackageManager packageManager = context.getPackageManager();
        final String appPackageName = context.getPackageName();
        final String installerPackageName = packageManager.getInstallerPackageName(appPackageName);
        return TextUtils.equals(installerPackageName, packageName);
    }

    /**
     * Convert intent to string.
     *
     * @return String representation of intent.
     */
    @Nullable
    public static String toString(@Nullable final Intent intent) {
        if (intent == null) {
            return "null";
        }

        final StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Intent{");
        stringBuilder.append("action=").append('"').append(intent.getAction()).append('"');
        stringBuilder.append(ITEM_DIVIDER);
        stringBuilder.append("data=").append('"').append(intent.getDataString()).append('"');
        stringBuilder.append(ITEM_DIVIDER);
        stringBuilder.append("component=").append('"').append(intent.getComponent()).append('"');
        stringBuilder.append(ITEM_DIVIDER);

        final Bundle extras = intent.getExtras();
        stringBuilder.append("extras=").append(extras == null ? null : toString(extras));
        stringBuilder.append('}');
        return stringBuilder.toString();
    }

    /**
     * Convert {@code Bundle} to string.
     *
     * @return String representation of bundles.
     */
    @NonNull
    public static String toString(@Nullable final Bundle bundle) {
        if (bundle == null) {
            return "null";
        }

        if (bundle.isEmpty()) {
            return "";
        }

        final StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append('[');
        for (String key : bundle.keySet()) {
            stringBuilder.append('"').append(key).append('"');
            stringBuilder.append(':');
            stringBuilder.append('"').append(bundle.get(key)).append('"');
            stringBuilder.append(ITEM_DIVIDER);
        }
        stringBuilder.setLength(stringBuilder.length() - ITEM_DIVIDER.length());
        stringBuilder.append(']');
        return stringBuilder.toString();
    }
}
