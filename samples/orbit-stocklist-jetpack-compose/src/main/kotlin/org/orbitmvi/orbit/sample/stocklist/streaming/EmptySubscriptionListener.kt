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
import com.lightstreamer.client.SubscriptionListener

/**
 * Empty SubscriptionListener
 */
@Suppress("TooManyFunctions")
object EmptySubscriptionListener : SubscriptionListener {
    override fun onListenEnd() = Unit

    override fun onItemUpdate(itemUpdate: ItemUpdate) = Unit

    override fun onSubscription() = Unit

    override fun onEndOfSnapshot(itemName: String?, itemPos: Int) = Unit

    override fun onItemLostUpdates(itemName: String?, itemPos: Int, lostUpdates: Int) = Unit

    override fun onSubscriptionError(code: Int, message: String?) = Unit

    override fun onClearSnapshot(itemName: String?, itemPos: Int) = Unit

    override fun onCommandSecondLevelSubscriptionError(code: Int, message: String?, key: String?) = Unit

    override fun onUnsubscription() = Unit

    override fun onCommandSecondLevelItemLostUpdates(lostUpdates: Int, key: String) = Unit

    override fun onListenStart() = Unit

    override fun onRealMaxFrequency(frequency: String?) = Unit
}
