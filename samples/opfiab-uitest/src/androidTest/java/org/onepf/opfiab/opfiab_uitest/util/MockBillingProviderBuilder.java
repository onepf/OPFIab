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
import org.onepf.opfiab.model.event.RequestHandledEvent;
import org.onepf.opfiab.model.event.billing.BillingRequest;
import org.onepf.opfiab.model.event.billing.PurchaseResponse;
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

    private final BillingProvider mock = mock(BillingProvider.class);

    private static final long SLEEP_TIME = 50L;

    public MockBillingProviderBuilder() {
        setWillPostSuccess(true);
    }

    public MockBillingProviderBuilder setWillPostSuccess(boolean willPostSuccess) {
        doAnswer(new MyAnswer(willPostSuccess)).when(mock).onEventAsync(any(BillingRequest.class));
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

    public BillingProvider build() {
        return mock;
    }

    private final class MyAnswer implements Answer<Void> {

        private final boolean willPostSuccess;

        public MyAnswer(boolean willPostSuccess) {
            this.willPostSuccess = willPostSuccess;
        }

        @Override
        public Void answer(final InvocationOnMock invocationOnMock) throws Throwable {
            final BillingRequest billingRequest = (BillingRequest)invocationOnMock.getArguments()[0];
            Thread.sleep(SLEEP_TIME);
            OPFIab.post(new RequestHandledEvent(billingRequest));
            if (willPostSuccess) {
                OPFIab.post(new PurchaseResponse(SUCCESS, mock.getInfo(), null, VerificationResult.SUCCESS));
            } else {
                OPFIab.post(OPFIabUtils.emptyResponse(mock.getInfo(), billingRequest,
                                                      BILLING_UNAVAILABLE));
            }
            return null;
        }
    }
}
