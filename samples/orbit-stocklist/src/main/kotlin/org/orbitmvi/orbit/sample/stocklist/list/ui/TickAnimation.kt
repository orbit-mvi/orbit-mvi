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

package org.orbitmvi.orbit.sample.stocklist.list.ui

import android.view.View
import android.widget.TextView
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.delay

fun animateChange(textView: TextView, tick: CheckableImageView, newValue: String, jobReference: JobHolder) {
    val currentValue = textView.text.toString()
    if (newValue != currentValue && currentValue.isNotEmpty()) {
        val diff = newValue.toDouble().compareTo(currentValue.toDouble())

        if (diff != 0) {
            tick.isChecked = diff > 0
            tick.visibility = View.VISIBLE

            jobReference.job?.cancel()

            jobReference.job = GlobalScope.async {
                @Suppress("MagicNumber")
                (delay(300))

                tick.visibility = View.INVISIBLE
            }
        }
    }
}

class JobHolder(var job: Job? = null)
