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

package org.onepf.opfiab.billing;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Handler;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.onepf.opfutils.OPFLog;
import org.onepf.opfutils.OPFUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public abstract class AidlBillingHelper<AIDL extends IInterface> implements ServiceConnection {

    private static final int CONNECTION_TIMEOUT = 3000; // 3 seconds
    private static final int DISCONNECT_DELAY = 60000; // 1 minute
    private static final Handler HANDLER = new Handler(Looper.getMainLooper());

    private final Semaphore serviceSemaphore = new Semaphore(0);
    private final Runnable disconnect = new Runnable() {
        @Override
        public void run() {
            if (service != null) {
                context.unbindService(AidlBillingHelper.this);
                service = null;
            }
        }
    };
    @NonNull
    protected final Context context;
    @NonNull
    private final Method asInterface;
    @Nullable
    private volatile AIDL service;

    protected AidlBillingHelper(@NonNull final Context context, @NonNull final Class<AIDL> clazz) {
        this.context = context.getApplicationContext();
        final Class<?>[] classes = clazz.getDeclaredClasses();
        for (final Class<?> declaredClass : classes) {
            if ("Stub".equals(declaredClass.getSimpleName()) &&
                    clazz.isAssignableFrom(declaredClass)) {
                try {
                    asInterface = declaredClass.getDeclaredMethod("asInterface", IBinder.class);
                    return;
                } catch (NoSuchMethodException e) {
                    throw new IllegalStateException(e);
                }
            }
        }
        throw new IllegalStateException("Couldn't extract Stub implementation from AIDL class.");
    }

    private void scheduleDisconnect() {
        HANDLER.removeCallbacks(disconnect);
        HANDLER.postDelayed(disconnect, DISCONNECT_DELAY);
    }

    @NonNull
    protected abstract Intent getServiceIntent();

    @Nullable
    public AIDL getService() {
        final AIDL service = this.service;
        if (service != null) {
            scheduleDisconnect();
            return service;
        }

        final Intent serviceIntent = getServiceIntent();
        final PackageManager packageManager = context.getPackageManager();
        final Collection<ResolveInfo> infos = packageManager.queryIntentServices(serviceIntent, 0);
        if (infos == null || infos.isEmpty()
                || !context.bindService(getServiceIntent(), this, Context.BIND_AUTO_CREATE)) {
            OPFLog.d("Can't bind to service: %s", asInterface.getDeclaringClass());
            return null;
        }
        serviceSemaphore.drainPermits();
        try {
            if (!serviceSemaphore.tryAcquire(CONNECTION_TIMEOUT, TimeUnit.MILLISECONDS)) {
                OPFLog.e("AIDL service connection timeout: %s", OPFUtils.toString(serviceIntent));
            }
        } catch (InterruptedException exception) {
            OPFLog.d("", exception);
        }
        return this.service;
    }

    @Override
    public void onServiceConnected(final ComponentName name, final IBinder service) {
        try {
            //noinspection unchecked
            this.service = (AIDL) asInterface.invoke(null, service);
        } catch (IllegalAccessException | InvocationTargetException exception) {
            OPFLog.e("", exception);
        }
        scheduleDisconnect();
        serviceSemaphore.release();
    }

    @Override
    public void onServiceDisconnected(final ComponentName name) {
        service = null;
    }
}
