package io.opentelemetry.kotlin.config.yaml

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.config.ConfigProperties

/**
 * A [ConfigProperties] view over a raw mapping node produced by [YamlConfigParser].
 *
 * Accessors are defensive on purpose: this SDK is embedded in host applications, so a
 * malformed or unexpectedly-typed value must degrade to `null` rather than throw. Numeric and
 * boolean getters accept both a [Number]/[Boolean] value (what yamlkt is observed to produce
 * today) and a parseable [String], so this class keeps working across a yamlkt upgrade that
 * changes its internal representation.
 *
 * Known limitation: the
 * [declarative configuration spec](https://opentelemetry.io/docs/specs/otel/configuration/api/)
 * asks implementations to let callers distinguish a property that is present with a `null`
 * value from one that is absent entirely. The [ConfigProperties] interface returns `T?`
 * uniformly for both cases, so [propertyKeys] is currently the only way to tell them apart.
 * This is a known limitation left to a follow-up.
 */
@ExperimentalApi
class MapConfigProperties(private val node: Map<String, Any?>) : ConfigProperties {

    override fun getString(name: String): String? =
        when (val value = node[name]) {
            null -> null
            is String -> value
            is Map<*, *>, is List<*> -> null
            else -> value.toString()
        }

    override fun getBoolean(name: String): Boolean? = asBoolean(node[name])

    override fun getLong(name: String): Long? = asLong(node[name])

    override fun getDouble(name: String): Double? = asDouble(node[name])

    override fun getStringList(name: String): List<String>? =
        asList(node[name])?.mapNotNull { element ->
            when (element) {
                null -> null
                is String -> element
                is Map<*, *>, is List<*> -> null
                else -> element.toString()
            }
        }

    override fun getBooleanList(name: String): List<Boolean>? =
        asList(node[name])?.mapNotNull(::asBoolean)

    override fun getLongList(name: String): List<Long>? =
        asList(node[name])?.mapNotNull(::asLong)

    override fun getDoubleList(name: String): List<Double>? =
        asList(node[name])?.mapNotNull(::asDouble)

    override fun getStructured(name: String): ConfigProperties? =
        asMap(node[name])?.let(::MapConfigProperties)

    override fun getStructuredList(name: String): List<ConfigProperties>? =
        asList(node[name])?.mapNotNull { element -> asMap(element)?.let(::MapConfigProperties) }

    override val propertyKeys: Set<String>
        get() = node.keys

    private fun asList(value: Any?): List<*>? = value as? List<*>

    companion object {
        /** The empty [ConfigProperties] used when a mapping node is absent. */
        val EMPTY: ConfigProperties = MapConfigProperties(emptyMap())

        private fun asBoolean(value: Any?): Boolean? =
            when (value) {
                is Boolean -> value
                is String ->
                    when (value.lowercase()) {
                        "true" -> true
                        "false" -> false
                        else -> null
                    }
                else -> null
            }

        private fun asLong(value: Any?): Long? =
            when (value) {
                is Number -> value.toLong()
                is String -> value.toLongOrNull() ?: value.toDoubleOrNull()?.toLong()
                else -> null
            }

        private fun asDouble(value: Any?): Double? =
            when (value) {
                is Number -> value.toDouble()
                is String -> value.toDoubleOrNull()
                else -> null
            }

        /**
         * Converts [value] to a `Map<String, Any?>` if it is a mapping node, dropping any
         * entries whose key is not a [String] rather than performing an unchecked cast.
         */
        private fun asMap(value: Any?): Map<String, Any?>? =
            (value as? Map<*, *>)?.toStringKeyedMap()
    }
}

/**
 * Converts a raw `Map<*, *>` into a `Map<String, Any?>`, keeping only entries whose key is a
 * [String] rather than performing an unchecked cast that could throw.
 */
fun Map<*, *>.toStringKeyedMap(): Map<String, Any?> =
    entries.mapNotNull { (key, value) -> (key as? String)?.let { it to value } }.toMap()

/**
 * Converts a nullable raw mapping node into a non-null [ConfigProperties], per the spec's
 * requirement that an absent `.instrumentation` node yield an empty [ConfigProperties] rather
 * than `null`.
 */
fun Map<String, Any?>?.toConfigProperties(): ConfigProperties =
    this?.let(::MapConfigProperties) ?: MapConfigProperties.EMPTY
