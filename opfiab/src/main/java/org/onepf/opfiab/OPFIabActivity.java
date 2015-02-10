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

package org.onepf.opfiab;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;

import org.onepf.opfiab.model.event.ActivityLifecycleEvent;
import org.onepf.opfiab.model.event.ActivityResultEvent;
import org.onepf.opfutils.OPFLog;

import de.greenrobot.event.EventBus;

import static org.onepf.opfiab.model.event.LifecycleEvent.Type.CREATE;
import static org.onepf.opfiab.model.event.LifecycleEvent.Type.DESTROY;
import static org.onepf.opfiab.model.event.LifecycleEvent.Type.PAUSE;
import static org.onepf.opfiab.model.event.LifecycleEvent.Type.RESUME;
import static org.onepf.opfiab.model.event.LifecycleEvent.Type.START;
import static org.onepf.opfiab.model.event.LifecycleEvent.Type.STOP;

public class OPFIabActivity extends Activity {

    protected static final int FINISH_DELAY = 3000;

    public static void start(@NonNull final Context context) {
        final Context applicationContext = context.getApplicationContext();
        final Intent intent = new Intent(applicationContext, OPFIabActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        applicationContext.startActivity(intent);
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
        handler.postDelayed(finishTask, FINISH_DELAY);
        OPFIab.post(new ActivityLifecycleEvent(CREATE, this));
    }

    @Override
    public void startActivityForResult(final Intent intent, final int requestCode) {
        handler.removeCallbacks(finishTask);
        super.startActivityForResult(intent, requestCode);
    }

    @Override
    public void startIntentSenderForResult(final IntentSender intent, final int requestCode,
                                           final Intent fillInIntent, final int flagsMask,
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
        super.onPause();
        OPFIab.post(new ActivityLifecycleEvent(PAUSE, this));
    }

    @Override
    protected void onStop() {
        super.onStop();
        OPFIab.post(new ActivityLifecycleEvent(STOP, this));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(finishTask);
        OPFIab.post(new ActivityLifecycleEvent(DESTROY, this));
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode,
                                    final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        OPFIab.post(new ActivityResultEvent(this, requestCode, resultCode, data));
        finish();
    }
}
