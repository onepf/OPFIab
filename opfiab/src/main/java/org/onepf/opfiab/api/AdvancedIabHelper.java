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

package org.onepf.opfiab.api;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import org.onepf.opfiab.OPFIab;
import org.onepf.opfiab.billing.BillingProvider;
import org.onepf.opfiab.listener.BillingListener;
import org.onepf.opfiab.listener.OnConsumeListener;
import org.onepf.opfiab.listener.OnInventoryListener;
import org.onepf.opfiab.listener.OnPurchaseListener;
import org.onepf.opfiab.listener.OnSetupListener;
import org.onepf.opfiab.listener.OnSkuDetailsListener;
import org.onepf.opfiab.model.event.billing.BillingRequest;
import org.onepf.opfiab.model.event.billing.BillingResponse;

/**
 * Advanced version of {@link SimpleIabHelper} with following features:
 * <ul>
 * <li>Request queue: If {@link BillingProvider} is busy with another request, new request will
 * be enqueued instead of immediately returning {@link org.onepf.opfiab.model.event.billing.Status#BUSY}.
 * <li>Lazy setup: If {@link OPFIab#setup()} wasn't called up to the point when this helper tries to
 * send {@link BillingRequest} - request will be enqueued and setup() will be called instead.
 * <li>Listeners API for handling {@link BillingResponse}s as well as setup events.
 * </ul>
 *
 * @see OnPurchaseListener
 * @see OnConsumeListener
 * @see OnInventoryListener
 * @see OnSkuDetailsListener
 * @see OnSetupListener
 * @see BillingListener
 */
public interface AdvancedIabHelper extends SimpleIabHelper, ListenersSupport {

    /**
     * Registers all listeners associated with this helper to receive appropriate callbacks.
     * <p/>
     * In case of activity best called from {@link Activity#onCreate(Bundle)}
     * <br>
     * In case of fragment best called from {@link Fragment#onCreateView(LayoutInflater, ViewGroup, Bundle)}
     *
     * @see #unregister()
     */
    void register();

    /**
     * Unregisters all listeners associated with this helpers from receiving any callbacks.
     * <p/>
     * All pending requests from this helper <b>will be dropped</b>.
     * <p/>
     * In case of activity best called from {@link Activity#onDestroy()}
     * <br>
     * In case of fragment best called from {@link Fragment#onDestroyView()}
     *
     * @see #register()
     */
    void unregister();
}
