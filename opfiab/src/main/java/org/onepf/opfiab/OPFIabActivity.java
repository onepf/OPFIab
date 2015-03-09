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

package org.onepf.opfiab;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.onepf.opfiab.model.event.ActivityLifecycleEvent;
import org.onepf.opfiab.model.event.ActivityResultEvent;
import org.onepf.opfiab.model.event.billing.BillingRequest;
import org.onepf.opfutils.OPFLog;

import static org.onepf.opfiab.model.ComponentState.CREATE;
import static org.onepf.opfiab.model.ComponentState.DESTROY;
import static org.onepf.opfiab.model.ComponentState.PAUSE;
import static org.onepf.opfiab.model.ComponentState.RESUME;
import static org.onepf.opfiab.model.ComponentState.START;
import static org.onepf.opfiab.model.ComponentState.STOP;

@SuppressLint("Registered")
public class OPFIabActivity extends Activity {

    protected static final int FINISH_DELAY = 3000;

    public static void start(@Nullable final Bundle bundle) {
        final Context context = OPFIab.getContext();
        final Intent intent = new Intent(context, OPFIabActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (bundle != null) {
            intent.putExtras(bundle);
        }
        context.startActivity(intent);
    }

    public static void start(@NonNull final BillingRequest request) {
        final Bundle bundle = new Bundle();
        OPFIabUtils.putRequest(bundle, request);
        start(bundle);
    }

    public static void start() {
        start((Bundle) null);
    }


    @NonNull
    protected final Handler handler = new Handler(Looper.getMainLooper());
    @NonNull
    protected final Runnable finishTask = new Runnable() {
        @Override
        public void run() {
            OPFLog.e("OPFIabActivity wasn't utilised! Finishing...");
            finish();
        }
    };

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final BillingRequest billingRequest = OPFIabUtils.getRequest(getIntent().getExtras());
        if (billingRequest != null) {
            final BillingRequest request = OPFIabUtils.withActivity(billingRequest, this);
            OPFIab.getBaseHelper().postRequest(request);
            finish();
        } else {
            handler.postDelayed(finishTask, FINISH_DELAY);
            OPFIab.post(new ActivityLifecycleEvent(CREATE, this));
        }
    }

    @Override
    public void startActivityForResult(final Intent intent, final int requestCode) {
        handler.removeCallbacks(finishTask);
        super.startActivityForResult(intent, requestCode);
    }

    @Override
    public void startIntentSenderForResult(final IntentSender intent,
                                           final int requestCode,
                                           final Intent fillInIntent,
                                           final int flagsMask,
                                           final int flagsValues,
                                           final int extraFlags)
            throws IntentSender.SendIntentException {
        handler.removeCallbacks(finishTask);
        super.startIntentSenderForResult(intent, requestCode, fillInIntent, flagsMask, flagsValues,
                                         extraFlags);
    }

    @Override
    protected void onStart() {
        super.onStart();
        OPFIab.post(new ActivityLifecycleEvent(START, this));
    }

    @Override
    protected void onResume() {
        super.onResume();
        OPFIab.post(new ActivityLifecycleEvent(RESUME, this));
    }

    @Override
    protected void onPause() {
        OPFIab.post(new ActivityLifecycleEvent(PAUSE, this));
        super.onPause();
    }

    @Override
    protected void onStop() {
        OPFIab.post(new ActivityLifecycleEvent(STOP, this));
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacks(finishTask);
        OPFIab.post(new ActivityLifecycleEvent(DESTROY, this));
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(final int requestCode,
                                    final int resultCode,
                                    final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        OPFIab.post(new ActivityResultEvent(this, requestCode, resultCode, data));
        finish();
    }
}
