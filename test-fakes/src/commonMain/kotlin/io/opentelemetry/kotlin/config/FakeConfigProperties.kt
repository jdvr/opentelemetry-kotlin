package io.opentelemetry.kotlin.config

class FakeConfigProperties(val map: MutableMap<String, Any?> = mutableMapOf()) : ConfigProperties {

    override fun getString(name: String): String? = map[name] as? String

    override fun getBoolean(name: String): Boolean? = map[name] as? Boolean

    override fun getLong(name: String): Long? = when (val value = map[name]) {
        is Long -> value
        is Int -> value.toLong()
        else -> null
    }

    override fun getDouble(name: String): Double? = when (val value = map[name]) {
        is Double -> value
        is Number -> value.toDouble()
        else -> null
    }

    @Suppress("UNCHECKED_CAST")
    override fun getStringList(name: String): List<String>? = map[name] as? List<String>

    @Suppress("UNCHECKED_CAST")
    override fun getBooleanList(name: String): List<Boolean>? = map[name] as? List<Boolean>

    @Suppress("UNCHECKED_CAST")
    override fun getLongList(name: String): List<Long>? = map[name] as? List<Long>

    @Suppress("UNCHECKED_CAST")
    override fun getDoubleList(name: String): List<Double>? = map[name] as? List<Double>

    override fun getStructured(name: String): ConfigProperties? = map[name] as? ConfigProperties

    @Suppress("UNCHECKED_CAST")
    override fun getStructuredList(name: String): List<ConfigProperties>? = map[name] as? List<ConfigProperties>

    override val propertyKeys: Set<String>
        get() = map.keys
}
