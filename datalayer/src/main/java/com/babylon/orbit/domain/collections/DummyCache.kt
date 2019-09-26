/*
 * Copyright 2019 Babylon Partners Limited
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.babylon.orbit.domain.collections

import com.nytimes.android.external.cache3.Cache
import java.util.concurrent.Callable
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

open class DummyCache<K, V> : Cache<K, V> {
    override fun invalidateAll(keys: MutableIterable<*>?) = Unit

    override fun invalidateAll() = Unit

    override fun putAll(m: MutableMap<out K, out V>?) = Unit

    override fun cleanUp() = Unit

    override fun getAllPresent(keys: MutableIterable<*>?): MutableMap<K, V> = mutableMapOf()

    override fun size(): Long = 0L

    override fun invalidate(key: Any?) = Unit

    override fun getIfPresent(key: Any?): V? = null

    override fun asMap(): ConcurrentMap<K, V> = ConcurrentHashMap<K, V>()

    override fun put(key: K, value: V) = Unit

    override fun get(key: K, valueLoader: Callable<out V>?): V? = null
}
