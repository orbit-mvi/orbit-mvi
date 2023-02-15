/*
 * Copyright 2021-2022 Mikołaj Leszczyński & Appmattus Limited
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

import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerDecorator
import org.orbitmvi.orbit.internal.LazyCreateContainerDecorator
import org.orbitmvi.orbit.internal.TestContainerDecorator

internal fun <STATE : Any, SIDE_EFFECT : Any> Container<STATE, SIDE_EFFECT>.findOnCreate(): (STATE) -> Unit {
    return (this as? LazyCreateContainerDecorator<STATE, SIDE_EFFECT>)?.onCreate
        ?: (this as? ContainerDecorator<STATE, SIDE_EFFECT>)?.actual?.findOnCreate()
        ?: {}
}

internal fun <STATE : Any, SIDE_EFFECT : Any> Container<STATE, SIDE_EFFECT>.findTestContainer(): TestContainerDecorator<STATE, SIDE_EFFECT> {
    return (this as? TestContainerDecorator<STATE, SIDE_EFFECT>)
        ?: (this as? ContainerDecorator<STATE, SIDE_EFFECT>)?.actual?.findTestContainer()
        ?: error("No TestContainerDecorator found!")
}
