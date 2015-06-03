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

package org.onepf.opfiab.opfiab_uitest.util;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.onepf.opfiab.OPFIab;
import org.onepf.opfiab.billing.BillingProvider;
import org.onepf.opfiab.model.BillingProviderInfo;
import org.onepf.opfiab.model.billing.Purchase;
import org.onepf.opfiab.model.event.RequestHandledEvent;
import org.onepf.opfiab.model.event.billing.BillingRequest;
import org.onepf.opfiab.model.event.billing.ConsumeRequest;
import org.onepf.opfiab.model.event.billing.ConsumeResponse;
import org.onepf.opfiab.model.event.billing.InventoryRequest;
import org.onepf.opfiab.model.event.billing.InventoryResponse;
import org.onepf.opfiab.model.event.billing.PurchaseRequest;
import org.onepf.opfiab.model.event.billing.PurchaseResponse;
import org.onepf.opfiab.model.event.billing.SkuDetailsRequest;
import org.onepf.opfiab.model.event.billing.SkuDetailsResponse;
import org.onepf.opfiab.model.event.billing.Status;
import org.onepf.opfiab.util.OPFIabUtils;
import org.onepf.opfiab.verification.VerificationResult;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.onepf.opfiab.model.event.billing.Status.BILLING_UNAVAILABLE;
import static org.onepf.opfiab.model.event.billing.Status.SUCCESS;

/**
 * @author antonpp
 * @since 25.05.15
 */
public class MockBillingProviderBuilder {

    private static final long DEFAULT_SLEEP_TIME = 50L;
    private final BillingProvider mock = mock(BillingProvider.class);

    private long sleepTime = DEFAULT_SLEEP_TIME;
    private boolean willPostSuccess = true;

    public MockBillingProviderBuilder setWillPostSuccess(boolean willPostSuccess) {
        this.willPostSuccess = willPostSuccess;
        return this;
    }

    public MockBillingProviderBuilder setIsAuthorised(boolean isAuthorised) {
        when(mock.isAuthorised()).thenReturn(isAuthorised);
        return this;
    }

    public MockBillingProviderBuilder setInfo(BillingProviderInfo info) {
        when(mock.getInfo()).thenReturn(info);
        return this;
    }

    public MockBillingProviderBuilder setIsAvailable(boolean isAvailable) {
        when(mock.isAvailable()).thenReturn(isAvailable);
        return this;
    }

    public MockBillingProviderBuilder setSleepTime(long sleepTime) {
        this.sleepTime = sleepTime;
        return this;
    }

    public BillingProvider build() {
        doAnswer(new BillingAnswer(willPostSuccess, sleepTime)).when(mock).onEventAsync(
                any(BillingRequest.class));
        return mock;
    }

    private final class BillingAnswer implements Answer<Void> {

        private final boolean willPostSuccess;
        private final long sleepTime;

        public BillingAnswer(boolean willPostSuccess, final long sleepTime) {
            this.willPostSuccess = willPostSuccess;
            this.sleepTime = sleepTime;
        }

        @Override
        public Void answer(final InvocationOnMock invocationOnMock) throws Throwable {
            final BillingRequest billingRequest = (BillingRequest)invocationOnMock.getArguments()[0];
            Thread.sleep(sleepTime);
            switch (billingRequest.getType()) {
                case CONSUME:
                    return answerConsume((ConsumeRequest) billingRequest);
                case PURCHASE:
                    return answerPurchase((PurchaseRequest) billingRequest);
                case SKU_DETAILS:
                    return answerSkuDetails((SkuDetailsRequest) billingRequest);
                case INVENTORY:
                    return answerInventory((InventoryRequest) billingRequest);
                default:
                    throw new IllegalStateException();
            }
        }

        public Void answerConsume(ConsumeRequest request) {
            OPFIab.post(new RequestHandledEvent(request));
            if (willPostSuccess) {
                OPFIab.post(
                        new ConsumeResponse(Status.SUCCESS, mock.getInfo(), request.getPurchase()));
            } else {
                OPFIab.post(
                        OPFIabUtils.emptyResponse(mock.getInfo(), request, BILLING_UNAVAILABLE));
            }
            return null;
        }

        public Void answerPurchase(PurchaseRequest request) {
            OPFIab.post(new RequestHandledEvent(request));
            if (willPostSuccess) {
                OPFIab.post(new PurchaseResponse(SUCCESS, mock.getInfo(),
                                                 new Purchase(request.getSku()),
                                                 VerificationResult.SUCCESS));
            } else {
                OPFIab.post(
                        OPFIabUtils.emptyResponse(mock.getInfo(), request, BILLING_UNAVAILABLE));
            }
            return null;
        }

        public Void answerSkuDetails(SkuDetailsRequest request) {
            OPFIab.post(new RequestHandledEvent(request));
            if (willPostSuccess) {
                OPFIab.post(new SkuDetailsResponse(SUCCESS, mock.getInfo(), null));
            } else {
                OPFIab.post(
                        OPFIabUtils.emptyResponse(mock.getInfo(), request, BILLING_UNAVAILABLE));
            }
            return null;
        }

        public Void answerInventory(InventoryRequest request) {
            OPFIab.post(new RequestHandledEvent(request));
            if (willPostSuccess) {
                OPFIab.post(new InventoryResponse(SUCCESS, mock.getInfo(), null, false));
            } else {
                OPFIab.post(
                        OPFIabUtils.emptyResponse(mock.getInfo(), request, BILLING_UNAVAILABLE));
            }
            return null;
        }
    }
}
