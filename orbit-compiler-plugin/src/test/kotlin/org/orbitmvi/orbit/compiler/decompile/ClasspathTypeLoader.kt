/*
 * Copyright 2023 Mikołaj Leszczyński & Appmattus Limited
 * Copyright 2013 Mike Strobel
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
 *
 * File modified by Mikołaj Leszczyński & Appmattus Limited
 */

package org.orbitmvi.orbit.compiler.decompile

import com.strobel.assembler.metadata.Buffer
import com.strobel.assembler.metadata.ITypeLoader
import java.io.IOException
import java.util.logging.Level
import java.util.logging.Logger

/**
 * Allow using a custom [ClassLoader] with procyon. The implementation in procyon does not allow
 * setting a custom [ClassLoader].
 */
class ClasspathTypeLoader(private val loader: ClassLoader) : ITypeLoader {

    override fun tryLoadType(internalName: String, buffer: Buffer): Boolean {
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("Attempting to load type: $internalName...")
        }
        val path = "$internalName.class"
        val resource = loader.getResource(path) ?: return false
        try {
            loader.getResourceAsStream(path)?.use { stream ->
                val temp = ByteArray(4096)
                var bytesRead: Int
                while (stream.read(temp, 0, temp.size).also { bytesRead = it } > 0) {
                    buffer.putByteArray(temp, 0, bytesRead)
                }
                buffer.flip()
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.fine("Type loaded from $resource.")
                }
                return true
            }
        } catch (ignored: IOException) {
        }
        return false
    }

    companion object {
        private val LOG = Logger.getLogger(ClasspathTypeLoader::class.java.simpleName)
    }
}
