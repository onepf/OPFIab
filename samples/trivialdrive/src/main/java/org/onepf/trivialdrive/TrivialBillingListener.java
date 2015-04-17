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

package org.onepf.trivialdrive;

import android.content.Context;
import android.support.annotation.NonNull;
import android.widget.Toast;

import org.onepf.opfiab.listener.DefaultBillingListener;
import org.onepf.opfiab.model.billing.Purchase;
import org.onepf.opfiab.model.event.billing.BillingResponse;
import org.onepf.opfiab.model.event.billing.ConsumeResponse;
import org.onepf.opfiab.model.event.billing.Status;
import org.onepf.opfiab.trivialdrive.R;

public class TrivialBillingListener extends DefaultBillingListener {

    private final Context context;

    public TrivialBillingListener(final Context context) {
        super();
        this.context = context.getApplicationContext();
    }

    @Override
    public void onResponse(@NonNull final BillingResponse billingResponse) {
        super.onResponse(billingResponse);
        if (!billingResponse.isSuccessful()) {
            final BillingResponse.Type type = billingResponse.getType();
            final Status status = billingResponse.getStatus();
            final String msg = context.getString(R.string.msg_request_failed, type, status);
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void consume(final Purchase purchase) {
        if (TrivialData.canAddGas()) {
            super.consume(purchase);
        }
    }

    @Override
    public void onConsume(@NonNull final ConsumeResponse consumeResponse) {
        super.onConsume(consumeResponse);
        if (consumeResponse.isSuccessful()
                && TrivialData.canAddGas()) {
            TrivialData.addGas();
        }
    }
}
