package io.opentelemetry.kotlin.config.yaml

import okio.Path.Companion.toPath
import okio.fakefilesystem.FakeFileSystem
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class InstrumentationNodeReaderTest {

    @Test
    fun returnsInstrumentationMapping() {
        val yaml =
            """
            instrumentation:
              general:
                peer:
                  service_mapping: []
            """.trimIndent()

        val result = readInstrumentationNode(yaml)
        assertEquals(setOf("general"), result?.keys)
    }

    @Test
    fun missingInstrumentationKeyReturnsNull() {
        assertNull(readInstrumentationNode("other: value"))
    }

    @Test
    fun scalarInstrumentationValueReturnsNull() {
        assertNull(readInstrumentationNode("instrumentation: enabled"))
    }

    @Test
    fun listInstrumentationValueReturnsNull() {
        assertNull(readInstrumentationNode("instrumentation: [a, b]"))
    }

    @Test
    fun emptyDocumentReturnsNull() {
        assertNull(readInstrumentationNode(""))
    }

    @Test
    fun bareScalarDocumentReturnsNull() {
        assertNull(readInstrumentationNode("just a string"))
    }

    @Test
    fun readsFromFileSystem() {
        val fileSystem = FakeFileSystem()
        val path = "config.yaml".toPath()
        fileSystem.write(path) {
            writeUtf8("instrumentation:\n  general: {}")
        }

        val result = readInstrumentationNode(fileSystem, path)
        assertEquals(setOf("general"), result?.keys)
    }

    @Test
    fun instrumentationDevelopmentKeyIsNotUsed() {
        // Deliberate: the generated schema model's @SerialName("instrumentation/development")
        // is intentionally not consulted here, only the exact key "instrumentation".
        assertNull(readInstrumentationNode("instrumentation/development:\n  general: {}"))
    }
}
