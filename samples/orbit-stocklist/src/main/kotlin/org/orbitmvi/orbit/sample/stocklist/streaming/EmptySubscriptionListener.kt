/*
 * Copyright (c) Lightstreamer Srl
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.orbitmvi.orbit.sample.stocklist.streaming

import com.lightstreamer.client.ItemUpdate
import com.lightstreamer.client.Subscription
import com.lightstreamer.client.SubscriptionListener

/**
 * Empty SubscriptionListener
 */
@Suppress("TooManyFunctions")
object EmptySubscriptionListener : SubscriptionListener {
    override fun onListenEnd(p0: Subscription) = Unit

    override fun onItemUpdate(p0: ItemUpdate) = Unit

    override fun onSubscription() = Unit

    override fun onEndOfSnapshot(p0: String?, p1: Int) = Unit

    override fun onItemLostUpdates(p0: String?, p1: Int, p2: Int) = Unit

    override fun onSubscriptionError(p0: Int, p1: String?) = Unit

    override fun onClearSnapshot(p0: String?, p1: Int) = Unit

    override fun onCommandSecondLevelSubscriptionError(p0: Int, p1: String?, p2: String?) = Unit

    override fun onUnsubscription() = Unit

    override fun onCommandSecondLevelItemLostUpdates(p0: Int, p1: String) = Unit

    override fun onListenStart(p0: Subscription) = Unit

    override fun onRealMaxFrequency(p0: String?) = Unit
}
