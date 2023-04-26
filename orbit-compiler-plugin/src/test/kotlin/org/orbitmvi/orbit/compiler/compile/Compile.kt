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

package org.orbitmvi.orbit.compiler.compile

import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.orbitmvi.orbit.compiler.ContainerHostPluginTest
import org.orbitmvi.orbit.compiler.decompile.decompile
import kotlin.test.assertEquals

val intentNameRegex = "intent\\\$default\\([^,]+,\\s*[^,]+,\\s*([^,]+),".toRegex()
val containerHostNameRegex = "container\\\$default\\([^,]+,\\s*[^,]+,\\s*[^,]+,\\s*([^,]+),".toRegex()

@OptIn(ExperimentalCompilerApi::class)
inline fun <reified T> String.compile(
    @Suppress("DEPRECATION") plugin: org.jetbrains.kotlin.compiler.plugin.ComponentRegistrar? = null
): ContainerHostPluginTest.CompilationResult<T> {
    val kotlinSource = SourceFile.kotlin(
        name = "$this.kt",
        contents = ContainerHostPluginTest::class.java.getResourceAsStream("/$this.kt")!!.bufferedReader().readText()
    )
    val result = KotlinCompilation().apply {
        sources = listOf(kotlinSource)
        // Awaiting new version of Arrow Meta to be released which migrates to compilerPluginRegistrars
        @Suppress("DEPRECATION")
        componentRegistrars = listOfNotNull(plugin)
        inheritClassPath = true
        verbose = false
    }.compile()

    assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)

    return object : ContainerHostPluginTest.CompilationResult<T> {
        override val containerHost: T by lazy {
            result.classLoader
                .loadClass("org.orbitmvi.${this@compile}")
                .getDeclaredConstructor()
                .newInstance() as T
        }

        private val decompiledSource by lazy { result.classLoader.decompile(result) }

        override val intentNames: List<String> by lazy {
            decompiledSource.lines().filter { it.contains("intent\$default") }.mapNotNull {
                intentNameRegex.find(it)?.groupValues?.get(1)
            }.distinct()
        }

        override val containerHostNames: List<String> by lazy {
            decompiledSource.lines().filter { it.contains("container\$default") }.mapNotNull {
                containerHostNameRegex.find(it)?.groupValues?.get(1)
            }.distinct()
        }
    }
}
