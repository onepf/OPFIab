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

import android.app.Service;
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
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.onepf.opfutils.OPFLog;
import org.onepf.opfutils.OPFUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * Helper class intended to simplify interaction with {@link Service} declared using Android
 * Interface Definition Language (AIDL).
 *
 * @param <AIDL> AIDL class to bind to.
 */
public abstract class AidlBillingHelper<AIDL extends IInterface> implements ServiceConnection {

    private static final Handler HANDLER = new Handler(Looper.getMainLooper());
    /**
     * Timeout to wait before giving up on connecting to service.
     */
    private static final long CONNECTION_TIMEOUT = Long.parseLong("30000");
    /**
     * Automatically disconnect from service after this delay since last usage.
     */
    private static final long DISCONNECT_DELAY = Long.parseLong("60000");

    /**
     * Used to block library thread and wait for service to connect.
     */
    private final Semaphore serviceSemaphore = new Semaphore(0);
    /**
     * Task to be used to disconnect from service.
     */
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
    /**
     * Method that is used to connect to the service.
     */
    @NonNull
    private final Method asInterface;
    @Nullable
    private volatile AIDL service;

    protected AidlBillingHelper(@NonNull final Context context, @NonNull final Class<AIDL> clazz) {
        this.context = context.getApplicationContext();
        final Class<?>[] classes = clazz.getDeclaredClasses();
        for (final Class<?> declaredClass : classes) {
            if ("Stub".equals(declaredClass.getSimpleName())
                    && clazz.isAssignableFrom(declaredClass)) {
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

    /**
     * Schedules {@link #disconnect} after {@link #DISCONNECT_DELAY} milliseconds.
     */
    private void scheduleDisconnect() {
        HANDLER.removeCallbacks(disconnect);
        HANDLER.postDelayed(disconnect, getDisconnectDelay());
    }

    /**
     * Acquires intent to start {@link Service} with.
     *
     * @return new Intent suitable for {@link Context#startService(Intent)}.
     */
    @Nullable
    protected abstract Intent getServiceIntent();

    protected long getDisconnectDelay() {
        return DISCONNECT_DELAY;
    }

    /**
     * Blocking call to retrieve {@link IInterface} instance to interact with {@link Service}.
     *
     * @return {@link IInterface} instance if {@link Service} connection was successful, null otherwise.
     */
    @Nullable
    public AIDL getService(final long timeout) {
        final AIDL service = this.service;
        if (service != null) {
            scheduleDisconnect();
            return service;
        }

        final Intent serviceIntent = getServiceIntent();
        if (serviceIntent == null) {
            return null;
        }
        final PackageManager packageManager = context.getPackageManager();
        final Collection<ResolveInfo> infos = packageManager.queryIntentServices(serviceIntent, 0);
        serviceSemaphore.drainPermits();
        if (infos == null || infos.isEmpty()
                || !context.bindService(serviceIntent, this, Context.BIND_AUTO_CREATE)) {
            OPFLog.d("Can't bind to service: %s", asInterface.getDeclaringClass());
            return null;
        }
        try {
            if (serviceSemaphore.tryAcquire(timeout, TimeUnit.MILLISECONDS)) {
                return this.service;
            }
            OPFLog.e("AIDL service connection timeout: %s", OPFUtils.toString(serviceIntent));
        } catch (InterruptedException exception) {
            OPFLog.d("", exception);
        }
        return null;
    }

    /**
     * Same as {@link #getService(long)} but with default timeout.
     *
     * @see #CONNECTION_TIMEOUT
     */
    @Nullable
    public AIDL getService() {
        return getService(CONNECTION_TIMEOUT);
    }

    @CallSuper
    @Override
    public void onServiceConnected(final ComponentName name, final IBinder service) {
        //https://code.google.com/p/android/issues/detail?id=153406
        //noinspection TryWithIdenticalCatches
        try {
            //noinspection unchecked
            this.service = (AIDL) asInterface.invoke(null, service);
        } catch (IllegalAccessException exception) {
            OPFLog.e("", exception);
        } catch (InvocationTargetException exception) {
            OPFLog.e("", exception);
        }
        scheduleDisconnect();
        serviceSemaphore.release();
    }

    @CallSuper
    @Override
    public void onServiceDisconnected(final ComponentName name) {
        service = null;
    }
}
