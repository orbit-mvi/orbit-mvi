/*
 * Copyright 2020 Babylon Partners Limited
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

package com.babylon.orbit2.internal

import com.babylon.orbit2.Container
import com.babylon.orbit2.test
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import org.junit.jupiter.api.Test
import kotlin.random.Random

internal class ContainerThreadingTest {

    @Test
    fun `container can process a second action while the first is suspended`() {
        val container = RealContainer<Int, Nothing>(Random.nextInt(), CoroutineScope(Dispatchers.Default), Container.Settings())
        val observer = container.stateFlow.test()
        val newState = Random.nextInt()

        container.orbit {
            delay(Long.MAX_VALUE)
        }
        container.orbit {
            reduce { newState }
        }

        observer.awaitCount(2)
        container.currentState.shouldBe(newState)
    }
}
