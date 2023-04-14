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

package org.orbitmvi.orbit.compiler.decompile

import com.strobel.decompiler.Decompiler
import com.strobel.decompiler.DecompilerSettings
import com.strobel.decompiler.PlainTextOutput
import com.tschuchort.compiletesting.KotlinCompilation

/**
 * Use procyon to decompile the classes for the [KotlinCompilation.Result]
 */
fun ClassLoader.decompile(result: KotlinCompilation.Result): String {
    val settings = DecompilerSettings.javaDefaults().apply {
        typeLoader = ClasspathTypeLoader(this@decompile)
    }

    val output = PlainTextOutput()

    result.generatedFiles.filter { it.extension == "class" }.map {
        it.relativeTo(result.outputDirectory).path.replace("\\.class$".toRegex(), "")
    }.forEach { internalName ->
        Decompiler.decompile(internalName, output, settings)
        output.writeLine()
        output.writeLine()
    }

    return output.toString()
}
