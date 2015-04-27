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

package org.onepf.opfiab.android;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.view.WindowManager;

import org.onepf.opfiab.OPFIab;
import org.onepf.opfiab.model.ComponentState;
import org.onepf.opfiab.model.event.android.ActivityLifecycleEvent;
import org.onepf.opfiab.model.event.android.ActivityNewIntentEvent;
import org.onepf.opfiab.model.event.android.ActivityResultEvent;
import org.onepf.opfutils.OPFLog;

/**
 * Untouchable activity without UI.
 * <p/>
 * Intended to intercept {@link Activity#onActivityResult(int, int, Intent)} callback,
 * or to be used as Activity {@link Context} when one is not available.
 */
@SuppressLint("Registered")
public class OPFIabActivity extends Activity {

    /**
     * If activity is not expecting {@link #onActivityResult(int, int, Intent)} call, it will auto-finish after this timeout.
     */
    protected static final long FINISH_DELAY = 300L; // 0.3 second


    /**
     * Start new instance of this activity.
     *
     * @param context Can't be null. Context object witch will be used to start new instance of {@link OPFIabActivity}.
     *                If passed object is not <code>instanceof</code> {@link Activity},
     *                new activity will be started with {@link Intent#FLAG_ACTIVITY_NEW_TASK} flag.
     */
    public static void start(@NonNull final Context context) {
        final Intent intent = new Intent(context, OPFIabActivity.class);
        final int flags = Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
                | Intent.FLAG_ACTIVITY_NO_ANIMATION;
        if (context instanceof Activity) {
            intent.setFlags(flags);
        } else {
            intent.setFlags(flags | Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        context.startActivity(intent);
    }


    protected final Handler handler = new Handler(Looper.getMainLooper());
    /**
     * Used to finish activity if for some reason it wasn't used by library.
     */
    protected final Runnable finishTask = new Runnable() {
        @Override
        public void run() {
            if (!isFinishing()) {
                OPFLog.e("OPFIabActivity wasn't utilised! Finishing: %s", OPFIabActivity.this);
                finish();
            }
        }
    };


    /**
     * Used to schedule {@link #finishTask} call after timeout.
     * <br>
     * Resets timeout if task was already scheduled.
     *
     * @param schedule True if finish should be scheduled, false otherwise.
     */
    protected void scheduleFinish(final boolean schedule) {
        handler.removeCallbacks(finishTask);
        if (schedule) {
            handler.postDelayed(finishTask, FINISH_DELAY);
        }
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        OPFLog.d("onCreate: %s, task: %d", this, getTaskId());
        // Don't handle any touch events
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        OPFIab.post(new ActivityLifecycleEvent(ComponentState.CREATE, this));
        onNewIntent(getIntent());
    }

    @Override
    protected void onNewIntent(final Intent intent) {
        super.onNewIntent(intent);
        OPFLog.d("onNewIntent: %s, task: %d, Intent: %s", this, getTaskId(), intent);
        scheduleFinish(true);
        OPFIab.post(new ActivityNewIntentEvent(this, intent));
    }

    @Override
    protected void onDestroy() {
        OPFLog.d("onDestroy: %s, task: %d", this, getTaskId());
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(final int requestCode,
                                    final int resultCode,
                                    final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        OPFLog.d("onActivityResult: %s, task: %d", this, getTaskId());
        OPFIab.post(new ActivityResultEvent(this, requestCode, resultCode, data));
        if (data != null || resultCode == RESULT_OK) {
            // Result was delivered successfully, there's no further need for this activity.
            finish();
        } else {
            // May indicated that this activity is used for something else, for example - to start
            // another activity.
            scheduleFinish(true);
        }
    }

    @Override
    public void finish() {
        OPFLog.d("finish: %s, task: %d", this, getTaskId());
        scheduleFinish(false);
        super.finish();
    }

    @Override
    public void startActivityForResult(final Intent intent, final int requestCode) {
        scheduleFinish(false);
        super.startActivityForResult(intent, requestCode);
    }

    @Override
    public void startActivityForResult(final Intent intent, final int requestCode,
                                       final Bundle options) {
        scheduleFinish(false);
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
        scheduleFinish(false);
        super.startIntentSenderForResult(intent, requestCode, fillInIntent, flagsMask, flagsValues,
                                         extraFlags);
    }

    @Override
    public void startIntentSenderForResult(final IntentSender intent, final int requestCode,
                                           final Intent fillInIntent, final int flagsMask,
                                           final int flagsValues,
                                           final int extraFlags, final Bundle options)
            throws IntentSender.SendIntentException {
        scheduleFinish(false);
        super.startIntentSenderForResult(intent, requestCode, fillInIntent, flagsMask, flagsValues,
                                         extraFlags, options);
    }

    @Override
    public void onBackPressed() {
        // ignore
    }
}
