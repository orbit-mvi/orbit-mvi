/*
 * Copyright 2023-2025 Mikołaj Leszczyński & Appmattus Limited
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

package org.orbitmvi.orbit.test

import org.orbitmvi.orbit.OrbitContainer
import org.orbitmvi.orbit.OrbitContainerDecorator
import org.orbitmvi.orbit.annotation.OrbitInternal
import org.orbitmvi.orbit.internal.ExternalStateContainerAdapter
import org.orbitmvi.orbit.internal.LazyCreateContainerDecorator
import org.orbitmvi.orbit.internal.TestContainerDecorator
import org.orbitmvi.orbit.syntax.ContainerContext

@Suppress("DEPRECATION")
@OptIn(OrbitInternal::class)
internal fun <INTERNAL_STATE : Any, EXTERNAL_STATE : Any, SIDE_EFFECT : Any>
    OrbitContainer<INTERNAL_STATE, EXTERNAL_STATE, SIDE_EFFECT>.findOnCreate():
    suspend ContainerContext<INTERNAL_STATE, SIDE_EFFECT>.() -> Unit {
    return (this as? LazyCreateContainerDecorator<INTERNAL_STATE, EXTERNAL_STATE, SIDE_EFFECT>)?.onCreate
        ?: (this as? OrbitContainerDecorator<INTERNAL_STATE, EXTERNAL_STATE, SIDE_EFFECT>)?.actual?.findOnCreate()
        ?: (this as? ExternalStateContainerAdapter<INTERNAL_STATE, EXTERNAL_STATE, SIDE_EFFECT>)?.delegate?.findOnCreate()
        ?: {}
}

@Suppress("DEPRECATION", "UNCHECKED_CAST")
@OptIn(OrbitInternal::class)
internal fun <INTERNAL_STATE : Any, EXTERNAL_STATE : Any, SIDE_EFFECT : Any>
    OrbitContainer<INTERNAL_STATE, EXTERNAL_STATE, SIDE_EFFECT>.findTestContainer():
    TestContainerDecorator<INTERNAL_STATE, EXTERNAL_STATE, SIDE_EFFECT> {
    return (this as? TestContainerDecorator<INTERNAL_STATE, EXTERNAL_STATE, SIDE_EFFECT>)
        ?: (this as? OrbitContainerDecorator<INTERNAL_STATE, EXTERNAL_STATE, SIDE_EFFECT>)?.actual?.findTestContainer()
        ?: (this as? ExternalStateContainerAdapter<INTERNAL_STATE, EXTERNAL_STATE, SIDE_EFFECT>)
            ?.delegate?.findTestContainer()
            as? TestContainerDecorator<INTERNAL_STATE, EXTERNAL_STATE, SIDE_EFFECT>
        ?: error("No TestContainerDecorator found!")
}
