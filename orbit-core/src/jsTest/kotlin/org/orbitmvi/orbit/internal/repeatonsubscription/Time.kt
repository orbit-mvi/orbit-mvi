package org.orbitmvi.orbit.internal.repeatonsubscription

import kotlin.js.Date
import kotlin.time.Duration.Companion.milliseconds

internal actual fun getSystemTimeInMillis() = Date.now().milliseconds.inWholeMilliseconds
