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

package org.onepf.opfiab.misc;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.view.WindowManager;

import org.onepf.opfiab.OPFIab;
import org.onepf.opfiab.model.ComponentState;
import org.onepf.opfiab.model.event.android.ActivityIntentEvent;
import org.onepf.opfiab.model.event.android.ActivityLifecycleEvent;
import org.onepf.opfiab.model.event.android.ActivityResultEvent;
import org.onepf.opfutils.OPFLog;

import java.util.concurrent.atomic.AtomicInteger;

@SuppressLint("Registered")
public class OPFIabActivity extends Activity {

    protected static final int FINISH_DELAY = 1000; // 1 second

    // don't have to be atomic, but oh well...
    private final AtomicInteger expectedResultsCounter = new AtomicInteger(0);

    public static void start(@Nullable final Activity activity, @Nullable final Bundle bundle) {
        final Context context = activity == null ? OPFIab.getContext() : activity;
        final Intent intent = new Intent(context, OPFIabActivity.class);
        if (bundle != null) {
            intent.putExtras(bundle);
        }
        final int flags = Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
                | Intent.FLAG_ACTIVITY_NO_ANIMATION
                | Intent.FLAG_ACTIVITY_NO_USER_ACTION;
        if (activity == null) {
            intent.setFlags(flags | Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        context.startActivity(intent);
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

    private void expectResult() {
        expectedResultsCounter.incrementAndGet();
        handler.removeCallbacks(finishTask);
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        OPFLog.d("onCreate: %s, task: %d", this, getTaskId());
        start(null, null);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

        OPFIab.post(new ActivityLifecycleEvent(ComponentState.CREATE, this));
        handler.postDelayed(finishTask, FINISH_DELAY);
        if (savedInstanceState == null) {
            onNewIntent(getIntent());
        }
    }

    @Override
    protected void onNewIntent(final Intent intent) {
        super.onNewIntent(intent);
        OPFLog.d("onNewIntent: %s, Intent: %s", this, intent);
        OPFIab.post(new ActivityIntentEvent(this, intent));
    }

    @Override
    public void finish() {
        OPFLog.d("finish: %s", this);
        handler.removeCallbacks(finishTask);
        super.finish();
    }

    @Override
    public void startActivityForResult(final Intent intent, final int requestCode) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            expectResult();
        }
        super.startActivityForResult(intent, requestCode);
    }

    @Override
    public void startActivityForResult(final Intent intent, final int requestCode,
                                       final Bundle options) {
        expectResult();
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
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            expectResult();
        }
        super.startIntentSenderForResult(intent, requestCode, fillInIntent, flagsMask, flagsValues,
                                         extraFlags);
    }

    @Override
    public void startIntentSenderForResult(final IntentSender intent, final int requestCode,
                                           final Intent fillInIntent, final int flagsMask,
                                           final int flagsValues,
                                           final int extraFlags, final Bundle options)
            throws IntentSender.SendIntentException {
        expectResult();
        super.startIntentSenderForResult(intent, requestCode, fillInIntent, flagsMask, flagsValues,
                                         extraFlags, options);
    }

    @Override
    protected void onActivityResult(final int requestCode,
                                    final int resultCode,
                                    final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        OPFIab.post(new ActivityResultEvent(this, requestCode, resultCode, data));
        if (expectedResultsCounter.decrementAndGet() == 0) {
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        // ignore
    }
}
