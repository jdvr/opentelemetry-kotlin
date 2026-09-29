package io.opentelemetry.kotlin.config.yaml

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

internal class MapConfigPropertiesTest {

    @Test
    fun absentKeyReturnsNull() {
        val properties = MapConfigProperties(emptyMap())
        assertNull(properties.getString("missing"))
        assertNull(properties.getBoolean("missing"))
        assertNull(properties.getLong("missing"))
        assertNull(properties.getDouble("missing"))
        assertNull(properties.getStringList("missing"))
        assertNull(properties.getBooleanList("missing"))
        assertNull(properties.getLongList("missing"))
        assertNull(properties.getDoubleList("missing"))
        assertNull(properties.getStructured("missing"))
        assertNull(properties.getStructuredList("missing"))
        assertTrue(properties.propertyKeys.isEmpty())
    }

    @Test
    fun nullValueReturnsNull() {
        val properties = MapConfigProperties(mapOf("key" to null))
        assertNull(properties.getString("key"))
        assertNull(properties.getBoolean("key"))
        assertNull(properties.getLong("key"))
        assertNull(properties.getDouble("key"))
        assertNull(properties.getStructured("key"))
        assertNull(properties.getStructuredList("key"))
    }

    @Test
    fun getStringOnStringScalar() {
        val properties = MapConfigProperties(mapOf("key" to "value"))
        assertEquals("value", properties.getString("key"))
    }

    @Test
    fun getStringOnNonStringScalarReturnsStringForm() {
        val properties = MapConfigProperties(mapOf("n" to 42, "b" to true, "d" to 42.5))
        assertEquals("42", properties.getString("n"))
        assertEquals("true", properties.getString("b"))
        assertEquals("42.5", properties.getString("d"))
    }

    @Test
    fun getStringOnMappingOrListReturnsNull() {
        val properties = MapConfigProperties(
            mapOf("m" to mapOf("a" to 1), "l" to listOf(1, 2))
        )
        assertNull(properties.getString("m"))
        assertNull(properties.getString("l"))
    }

    @Test
    fun getLongOnNumberTruncatesTowardZero() {
        val properties = MapConfigProperties(mapOf("i" to 42, "d" to 42.9))
        assertEquals(42L, properties.getLong("i"))
        assertEquals(42L, properties.getLong("d"))
    }

    @Test
    fun getLongOnParseableStringIsParsed() {
        val properties = MapConfigProperties(mapOf("key" to "42"))
        assertEquals(42L, properties.getLong("key"))
    }

    @Test
    fun getLongOnUnparseableValueReturnsNull() {
        val properties = MapConfigProperties(
            mapOf(
                "s" to "abc",
                "m" to mapOf("a" to 1),
                "l" to listOf(1, 2)
            )
        )
        assertNull(properties.getLong("s"))
        assertNull(properties.getLong("m"))
        assertNull(properties.getLong("l"))
    }

    @Test
    fun getDoubleOnNumberOrParseableString() {
        val properties = MapConfigProperties(mapOf("n" to 42, "d" to 42.5, "s" to "42.5"))
        assertEquals(42.0, properties.getDouble("n"))
        assertEquals(42.5, properties.getDouble("d"))
        assertEquals(42.5, properties.getDouble("s"))
    }

    @Test
    fun getDoubleOnUnparseableValueReturnsNull() {
        val properties = MapConfigProperties(mapOf("s" to "abc"))
        assertNull(properties.getDouble("s"))
    }

    @Test
    fun getBooleanOnBooleanOrParseableString() {
        val properties = MapConfigProperties(
            mapOf("b" to true, "sTrue" to "true", "sFalse" to "FALSE")
        )
        assertEquals(true, properties.getBoolean("b"))
        assertEquals(true, properties.getBoolean("sTrue"))
        assertEquals(false, properties.getBoolean("sFalse"))
    }

    @Test
    fun getBooleanOnUnparseableValueReturnsNull() {
        val properties = MapConfigProperties(mapOf("s" to "notabool", "n" to 1))
        assertNull(properties.getBoolean("s"))
        assertNull(properties.getBoolean("n"))
    }

    @Test
    fun getStructuredOnMapping() {
        val properties = MapConfigProperties(mapOf("child" to mapOf("key" to "value")))
        val child = properties.getStructured("child")
        assertEquals("value", child?.getString("key"))
    }

    @Test
    fun getStructuredOnNonMappingReturnsNull() {
        val properties = MapConfigProperties(mapOf("key" to "value", "list" to listOf(1)))
        assertNull(properties.getStructured("key"))
        assertNull(properties.getStructured("list"))
    }

    @Test
    fun getStructuredTwoLevelsDeep() {
        val properties = MapConfigProperties(
            mapOf(
                "level1" to mapOf(
                    "level2" to mapOf("key" to "value")
                )
            )
        )
        val level2 = properties.getStructured("level1")?.getStructured("level2")
        assertEquals("value", level2?.getString("key"))
    }

    @Test
    fun getStringListCoercesAndDropsUnconvertible() {
        val properties = MapConfigProperties(
            mapOf("list" to listOf("a", 1, true, mapOf("x" to 1), listOf(1)))
        )
        assertEquals(listOf("a", "1", "true"), properties.getStringList("list"))
    }

    @Test
    fun getBooleanListCoercesAndDropsUnconvertible() {
        val properties = MapConfigProperties(
            mapOf("list" to listOf(true, "false", "notabool", 1))
        )
        assertEquals(listOf(true, false), properties.getBooleanList("list"))
    }

    @Test
    fun getLongListCoercesAndDropsUnconvertible() {
        val properties = MapConfigProperties(
            mapOf("list" to listOf(1, "2", 3.9, "abc"))
        )
        assertEquals(listOf(1L, 2L, 3L), properties.getLongList("list"))
    }

    @Test
    fun getDoubleListCoercesAndDropsUnconvertible() {
        val properties = MapConfigProperties(
            mapOf("list" to listOf(1, "2.5", "abc"))
        )
        assertEquals(listOf(1.0, 2.5), properties.getDoubleList("list"))
    }

    @Test
    fun listGettersOnNonListReturnNull() {
        val properties = MapConfigProperties(mapOf("key" to "value"))
        assertNull(properties.getStringList("key"))
        assertNull(properties.getBooleanList("key"))
        assertNull(properties.getLongList("key"))
        assertNull(properties.getDoubleList("key"))
        assertNull(properties.getStructuredList("key"))
    }

    @Test
    fun getStructuredListMapsMappingsAndDropsOthers() {
        val properties = MapConfigProperties(
            mapOf("list" to listOf(mapOf("a" to 1), "not a mapping", mapOf("a" to 2)))
        )
        val structured = properties.getStructuredList("list")
        assertEquals(2, structured?.size)
        assertEquals(1L, structured?.get(0)?.getLong("a"))
        assertEquals(2L, structured?.get(1)?.getLong("a"))
    }

    @Test
    fun propertyKeysReturnsNodeKeys() {
        val properties = MapConfigProperties(mapOf("a" to 1, "b" to 2))
        assertEquals(setOf("a", "b"), properties.propertyKeys)
    }

    @Test
    fun mistypedNestedMapKeysAreFilteredSafely() {
        val rawNested: Map<*, *> = mapOf(1 to "one", "key" to "value")
        val properties = MapConfigProperties(mapOf("child" to rawNested))
        val child = properties.getStructured("child")
        assertEquals("value", child?.getString("key"))
        assertEquals(setOf("key"), child?.propertyKeys)
    }

    @Test
    fun toConfigPropertiesOnNullReceiverReturnsEmptyNonNull() {
        val properties: Map<String, Any?>? = null
        val result = properties.toConfigProperties()
        assertTrue(result.propertyKeys.isEmpty())
        assertNull(result.getString("any"))
    }

    @Test
    fun toConfigPropertiesOnPresentMapWrapsIt() {
        val result = mapOf("key" to "value").toConfigProperties()
        assertEquals("value", result.getString("key"))
    }
}
