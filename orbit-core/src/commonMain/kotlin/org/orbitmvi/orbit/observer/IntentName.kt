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

package org.orbitmvi.orbit.observer

import org.orbitmvi.orbit.annotation.OrbitExperimental

/**
 * Resolves a best-effort display name for an intent from its transformer lambda,
 * for reporting via [OrbitEventObserver.onIntentDispatched].
 *
 * On JVM targets the lambda's class name is parsed, so an intent declared in
 * `CheckoutViewModel.loadData()` resolves to `"CheckoutViewModel.loadData"`. Names degrade
 * under minification — keep the relevant proguard rules in instrumented builds or rely on
 * [org.orbitmvi.orbit.SettingsBuilder.containerName]. Other targets return `null`.
 *
 * Only invoked when an [OrbitEventObserver] is installed; adds no cost otherwise.
 *
 * @return the resolved name, or `null` when unavailable on this platform.
 */
@OrbitExperimental
public expect fun intentNameOf(transformer: Any): String?
