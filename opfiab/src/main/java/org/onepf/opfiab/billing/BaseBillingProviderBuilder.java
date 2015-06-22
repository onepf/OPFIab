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

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.onepf.opfiab.sku.SkuResolver;
import org.onepf.opfiab.verification.PurchaseVerifier;

/**
 * Builder class for this BillingProvider.
 *
 * @param <R> {@link SkuResolver} subclass to use with this BillingProvider.
 * @param <V> {@link PurchaseVerifier} subclass to use with this BillingProvider.
 */
@SuppressWarnings("unchecked")
public abstract class BaseBillingProviderBuilder<B extends BaseBillingProviderBuilder,
        R extends SkuResolver, V extends PurchaseVerifier> {

    @NonNull
    protected final Context context;
    @Nullable
    protected R skuResolver;
    @Nullable
    protected V purchaseVerifier;

    protected BaseBillingProviderBuilder(@NonNull final Context context) {
        this.context = context;
    }

    /**
     * Sets {@link SkuResolver} to use with this BillingProvider.
     *
     * @param skuResolver SkuResolver to use with this BillingProvider.
     *
     * @return this object.
     */
    public B setSkuResolver(@NonNull final R skuResolver) {
        this.skuResolver = skuResolver;
        return (B) this;
    }

    /**
     * Sets {@link PurchaseVerifier} to use with this BillingProvider.
     *
     * @param purchaseVerifier PurchaseVerifier to use with this BillingProvider.
     *
     * @return this object.
     */
    public B setPurchaseVerifier(@NonNull final V purchaseVerifier) {
        this.purchaseVerifier = purchaseVerifier;
        return (B) this;
    }

    /**
     * Constructs a new {@link BillingProvider} object.
     *
     * @return new BillingProvider.
     */
    public abstract BaseBillingProvider build();
}
