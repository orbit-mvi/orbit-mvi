/*
 * Copyright 2026 Mikołaj Leszczyński & Appmattus Limited
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

@file:Suppress("TooManyFunctions", "LongParameterList")

package org.orbitmvi.orbit.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import org.orbitmvi.orbit.OrbitContainerHost
import org.orbitmvi.orbit.annotation.OrbitExperimental
import org.orbitmvi.orbit.combine

// -------- ViewModel-scoped combine, no side-effects, arities 2..5 --------

@OrbitExperimental
public fun <ES1 : Any, ES2 : Any, R : Any> ViewModel.combine(
    host1: OrbitContainerHost<*, ES1, *>,
    host2: OrbitContainerHost<*, ES2, *>,
    transformState: (ES1, ES2) -> R,
): OrbitContainerHost<Unit, R, Nothing> =
    combine(viewModelScope, host1, host2, transformState)

@OrbitExperimental
public fun <ES1 : Any, ES2 : Any, ES3 : Any, R : Any> ViewModel.combine(
    host1: OrbitContainerHost<*, ES1, *>,
    host2: OrbitContainerHost<*, ES2, *>,
    host3: OrbitContainerHost<*, ES3, *>,
    transformState: (ES1, ES2, ES3) -> R,
): OrbitContainerHost<Unit, R, Nothing> =
    combine(viewModelScope, host1, host2, host3, transformState)

@OrbitExperimental
public fun <ES1 : Any, ES2 : Any, ES3 : Any, ES4 : Any, R : Any> ViewModel.combine(
    host1: OrbitContainerHost<*, ES1, *>,
    host2: OrbitContainerHost<*, ES2, *>,
    host3: OrbitContainerHost<*, ES3, *>,
    host4: OrbitContainerHost<*, ES4, *>,
    transformState: (ES1, ES2, ES3, ES4) -> R,
): OrbitContainerHost<Unit, R, Nothing> =
    combine(viewModelScope, host1, host2, host3, host4, transformState)

@OrbitExperimental
public fun <ES1 : Any, ES2 : Any, ES3 : Any, ES4 : Any, ES5 : Any, R : Any> ViewModel.combine(
    host1: OrbitContainerHost<*, ES1, *>,
    host2: OrbitContainerHost<*, ES2, *>,
    host3: OrbitContainerHost<*, ES3, *>,
    host4: OrbitContainerHost<*, ES4, *>,
    host5: OrbitContainerHost<*, ES5, *>,
    transformState: (ES1, ES2, ES3, ES4, ES5) -> R,
): OrbitContainerHost<Unit, R, Nothing> =
    combine(viewModelScope, host1, host2, host3, host4, host5, transformState)

// -------- ViewModel-scoped combine, with side-effect transform, arities 2..5 --------

@OrbitExperimental
public fun <ES1 : Any, SE1 : Any, ES2 : Any, SE2 : Any, R : Any, T : Any> ViewModel.combine(
    host1: OrbitContainerHost<*, ES1, SE1>,
    host2: OrbitContainerHost<*, ES2, SE2>,
    transformState: (ES1, ES2) -> R,
    transformSideEffects: suspend FlowCollector<T>.(Flow<SE1>, Flow<SE2>) -> Unit,
): OrbitContainerHost<Unit, R, T> =
    combine(viewModelScope, host1, host2, transformState, transformSideEffects)

@OrbitExperimental
public fun <ES1 : Any, SE1 : Any, ES2 : Any, SE2 : Any, ES3 : Any, SE3 : Any, R : Any, T : Any> ViewModel.combine(
    host1: OrbitContainerHost<*, ES1, SE1>,
    host2: OrbitContainerHost<*, ES2, SE2>,
    host3: OrbitContainerHost<*, ES3, SE3>,
    transformState: (ES1, ES2, ES3) -> R,
    transformSideEffects: suspend FlowCollector<T>.(Flow<SE1>, Flow<SE2>, Flow<SE3>) -> Unit,
): OrbitContainerHost<Unit, R, T> =
    combine(viewModelScope, host1, host2, host3, transformState, transformSideEffects)

@OrbitExperimental
public fun <
    ES1 : Any,
    SE1 : Any,
    ES2 : Any,
    SE2 : Any,
    ES3 : Any,
    SE3 : Any,
    ES4 : Any,
    SE4 : Any,
    R : Any,
    T : Any,
    > ViewModel.combine(
    host1: OrbitContainerHost<*, ES1, SE1>,
    host2: OrbitContainerHost<*, ES2, SE2>,
    host3: OrbitContainerHost<*, ES3, SE3>,
    host4: OrbitContainerHost<*, ES4, SE4>,
    transformState: (ES1, ES2, ES3, ES4) -> R,
    transformSideEffects: suspend FlowCollector<T>.(Flow<SE1>, Flow<SE2>, Flow<SE3>, Flow<SE4>) -> Unit,
): OrbitContainerHost<Unit, R, T> =
    combine(viewModelScope, host1, host2, host3, host4, transformState, transformSideEffects)

@OrbitExperimental
public fun <
    ES1 : Any,
    SE1 : Any,
    ES2 : Any,
    SE2 : Any,
    ES3 : Any,
    SE3 : Any,
    ES4 : Any,
    SE4 : Any,
    ES5 : Any,
    SE5 : Any,
    R : Any,
    T : Any,
    > ViewModel.combine(
    host1: OrbitContainerHost<*, ES1, SE1>,
    host2: OrbitContainerHost<*, ES2, SE2>,
    host3: OrbitContainerHost<*, ES3, SE3>,
    host4: OrbitContainerHost<*, ES4, SE4>,
    host5: OrbitContainerHost<*, ES5, SE5>,
    transformState: (ES1, ES2, ES3, ES4, ES5) -> R,
    transformSideEffects: suspend FlowCollector<T>.(Flow<SE1>, Flow<SE2>, Flow<SE3>, Flow<SE4>, Flow<SE5>) -> Unit,
): OrbitContainerHost<Unit, R, T> =
    combine(viewModelScope, host1, host2, host3, host4, host5, transformState, transformSideEffects)
