package io.opentelemetry.kotlin.config.yaml

import okio.Path.Companion.toPath
import okio.fakefilesystem.FakeFileSystem
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

internal class YamlConfigParserTest {

    private val parser = YamlConfigParser()

    @Test
    fun emptyStringReturnsNull() {
        assertNull(parser.parse(""))
    }

    @Test
    fun parsesScalarMapping() {
        val result = parser.parse("key: value")
        assertEquals(mapOf("key" to "value"), result)
    }

    @Test
    fun parsesNestedMapping() {
        val yaml =
            """
            parent:
              child: value
            """.trimIndent()
        val result = parser.parse(yaml)
        assertEquals(mapOf("parent" to mapOf("child" to "value")), result)
    }

    @Test
    fun emptyFileReturnsNull() {
        val fileSystem = FakeFileSystem()
        val path = "config.yaml".toPath()
        fileSystem.write(path) { }

        assertNull(parser.parse(fileSystem, path))
    }

    @Test
    fun parsesFileContents() {
        val fileSystem = FakeFileSystem()
        val path = "config.yaml".toPath()
        fileSystem.write(path) {
            writeUtf8("key: value")
        }

        assertEquals(mapOf("key" to "value"), parser.parse(fileSystem, path))
    }

    /**
     * Characterization test: pins down the Kotlin type yamlkt produces for an integer scalar.
     */
    @Test
    fun parsesIntegerScalar() {
        val result = parser.parse("n: 42") as Map<*, *>
        assertEquals(42, result["n"])
    }

    /**
     * Characterization test: pins down the Kotlin type yamlkt produces for a float scalar.
     */
    @Test
    fun parsesFloatScalar() {
        val result = parser.parse("d: 42.5") as Map<*, *>
        assertEquals(42.5, result["d"])
    }

    /**
     * Characterization test: pins down the Kotlin type yamlkt produces for a boolean scalar.
     */
    @Test
    fun parsesBooleanScalar() {
        val result = parser.parse("b: true") as Map<*, *>
        assertEquals(true, result["b"])
    }

    /**
     * Characterization test: a quoted numeric string stays a [String], not a number.
     */
    @Test
    fun parsesQuotedNumericStringScalar() {
        val result = parser.parse("s: \"42\"") as Map<*, *>
        assertEquals("42", result["s"])
    }

    /**
     * Characterization test: an explicit YAML null decodes to a `null` value with the key present.
     */
    @Test
    fun parsesExplicitNull() {
        val result = parser.parse("nil: null") as Map<*, *>
        assertTrue(result.containsKey("nil"))
        assertNull(result["nil"])
    }

    /**
     * Characterization test: a sequence of scalars decodes to a [List] of the same scalar types.
     */
    @Test
    fun parsesSequenceOfScalars() {
        val result = parser.parse("list: [1, 2, 3]") as Map<*, *>
        assertEquals(listOf(1, 2, 3), result["list"])
    }

    /**
     * Characterization test: a sequence of mappings decodes to a [List] of [Map]s.
     */
    @Test
    fun parsesSequenceOfMappings() {
        val yaml =
            """
            list:
              - a: 1
              - a: 2
            """.trimIndent()
        val result = parser.parse(yaml) as Map<*, *>
        assertEquals(listOf(mapOf("a" to 1), mapOf("a" to 2)), result["list"])
    }
}
