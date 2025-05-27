/*
 * Copyright 2023 Mikołaj Leszczyński & Appmattus Limited
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

public sealed class ItemWithExternalState<INTERNAL_STATE : Any, EXTERNAL_STATE : Any, SIDE_EFFECT : Any> {
    public data class InternalStateItem<INTERNAL_STATE : Any, EXTERNAL_STATE : Any, SIDE_EFFECT : Any>(
        val value: INTERNAL_STATE
    ) : ItemWithExternalState<INTERNAL_STATE, EXTERNAL_STATE, SIDE_EFFECT>()

    public data class ExternalStateItem<INTERNAL_STATE : Any, EXTERNAL_STATE : Any, SIDE_EFFECT : Any>(
        val value: EXTERNAL_STATE
    ) : ItemWithExternalState<INTERNAL_STATE, EXTERNAL_STATE, SIDE_EFFECT>()

    public data class SideEffectItem<INTERNAL_STATE : Any, EXTERNAL_STATE : Any, SIDE_EFFECT : Any>(
        val value: SIDE_EFFECT
    ) : ItemWithExternalState<INTERNAL_STATE, EXTERNAL_STATE, SIDE_EFFECT>()
}
