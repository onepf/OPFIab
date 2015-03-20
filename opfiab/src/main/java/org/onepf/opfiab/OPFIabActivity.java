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

import org.onepf.opfiab.model.ComponentState;
import org.onepf.opfiab.model.event.ActivityLifecycleEvent;
import org.onepf.opfiab.model.event.ActivityResultEvent;
import org.onepf.opfiab.model.event.billing.BillingRequest;
import org.onepf.opfutils.OPFLog;

@SuppressLint("Registered")
public class OPFIabActivity extends Activity {

    protected static final int FINISH_DELAY = 3000;

    public static void start(@Nullable final Activity activity, @Nullable final Bundle bundle) {
        final Context context = OPFIab.getContext();
        final Intent intent = new Intent(context, OPFIabActivity.class);
        if (bundle != null) {
            intent.putExtras(bundle);
        }
        final int flags = Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
                | Intent.FLAG_ACTIVITY_NO_ANIMATION
                | Intent.FLAG_ACTIVITY_NO_USER_ACTION;
        if (activity == null) {
            intent.setFlags(flags | Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } else {
            intent.setFlags(flags);
            activity.startActivity(intent);
        }
    }

    public static void start(@Nullable final Activity activity,
                             @NonNull final BillingRequest request) {
        final Bundle bundle = new Bundle();
        OPFIabUtils.putRequest(bundle, request);
        start(activity, bundle);
    }


    protected final Handler handler = new Handler(Looper.getMainLooper());
    protected final Runnable finishTask = new Runnable() {
        @Override
        public void run() {
            if (!isFinishing()) {
                OPFLog.e("OPFIabActivity wasn't utilised! Finishing...");
                finish();
            }
        }
    };

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        handler.postDelayed(finishTask, FINISH_DELAY);
        if (savedInstanceState == null) {
            OPFIab.post(new ActivityLifecycleEvent(ComponentState.CREATE, this));
        }
    }

    @Override
    public void finish() {
        handler.removeCallbacks(finishTask);
        super.finish();
    }

    @Override
    public void startActivityForResult(final Intent intent, final int requestCode) {
        handler.removeCallbacks(finishTask);
        super.startActivityForResult(intent, requestCode);
    }

    @Override
    public void startActivityForResult(final Intent intent, final int requestCode,
                                       final Bundle options) {
        handler.removeCallbacks(finishTask);
        super.startActivityForResult(intent, requestCode, options);
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
    public void startIntentSenderForResult(final IntentSender intent, final int requestCode,
                                           final Intent fillInIntent, final int flagsMask,
                                           final int flagsValues,
                                           final int extraFlags, final Bundle options)
            throws IntentSender.SendIntentException {
        handler.removeCallbacks(finishTask);
        super.startIntentSenderForResult(intent, requestCode, fillInIntent, flagsMask, flagsValues,
                                         extraFlags, options);
    }

    @Override
    protected void onActivityResult(final int requestCode,
                                    final int resultCode,
                                    final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        OPFIab.post(new ActivityResultEvent(this, requestCode, resultCode, data));
        finish();
    }

    @Override
    public void onBackPressed() {
        // ignore
    }
}
