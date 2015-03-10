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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public abstract class AidlBillingHelper<AIDL extends IInterface> implements ServiceConnection {

    private static final int CONNECTION_TIMEOUT = 3000;
    private static final int DISCONNECT_DELAY = 1 * 60 * 1000;
    private static final Handler HANDLER = new Handler(Looper.getMainLooper());

    private final Semaphore serviceSemaphore = new Semaphore(0);
    private final Runnable disconnect = new Runnable() {
        @Override
        public void run() {
            if (service != null) {
                context.unbindService(AidlBillingHelper.this);
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
        try {
            asInterface = clazz.getDeclaredMethod("asInterface", IBinder.class);
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException();
        }
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
            return null;
        }
        try {
            serviceSemaphore.tryAcquire(CONNECTION_TIMEOUT, TimeUnit.MILLISECONDS);
        } catch (InterruptedException exception) {
            OPFLog.e("", exception);
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
