package io.opentelemetry.kotlin.config

/**
 * A no-op [ConfigProvider] for the compat module.
 *
 * opentelemetry-java's equivalent, `DeclarativeConfigProperties`, lives in
 * `opentelemetry-api-incubator`, which this module does not depend on, and there is no alias
 * for it in `java-typealiases`. Until opentelemetry-java ships a stable Instrumentation
 * Configuration API that compat can delegate to, this always reports an empty configuration.
 *
 * TODO: delegate to opentelemetry-java's `DeclarativeConfigProperties` once that API is stable
 * and reachable from this module.
 */
internal object CompatConfigProvider : ConfigProvider {
    override val instrumentationConfig: ConfigProperties = CompatConfigProperties
}

private object CompatConfigProperties : ConfigProperties {
    override fun getString(name: String): String? = null
    override fun getBoolean(name: String): Boolean? = null
    override fun getLong(name: String): Long? = null
    override fun getDouble(name: String): Double? = null
    override fun getStringList(name: String): List<String>? = null
    override fun getBooleanList(name: String): List<Boolean>? = null
    override fun getLongList(name: String): List<Long>? = null
    override fun getDoubleList(name: String): List<Double>? = null
    override fun getStructured(name: String): ConfigProperties? = null
    override fun getStructuredList(name: String): List<ConfigProperties>? = null
    override val propertyKeys: Set<String> = emptySet()
}
