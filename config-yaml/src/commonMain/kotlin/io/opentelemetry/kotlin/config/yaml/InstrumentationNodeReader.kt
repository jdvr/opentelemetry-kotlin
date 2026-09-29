package io.opentelemetry.kotlin.config.yaml

import io.opentelemetry.kotlin.ExperimentalApi
import okio.FileSystem
import okio.Path

/**
 * The declarative-configuration key holding the instrumentation-library configuration section.
 *
 * Deliberately `instrumentation`, not `instrumentation/development` — the generated schema
 * model's `@SerialName("instrumentation/development")` is intentionally not used here.
 */
private const val INSTRUMENTATION_KEY = "instrumentation"

/**
 * Reads the raw `.instrumentation` mapping node out of the declarative config file at [path],
 * for later use as a [io.opentelemetry.kotlin.config.ConfigProperties] source. Returns `null`
 * if the document is not a mapping, the key is absent, or its value is not itself a mapping.
 */
@ExperimentalApi
fun readInstrumentationNode(
    fileSystem: FileSystem,
    path: Path,
    parser: YamlConfigParser = YamlConfigParser(),
): Map<String, Any?>? = extractInstrumentationNode(parser.parse(fileSystem, path))

/**
 * Reads the raw `.instrumentation` mapping node out of a YAML document given as [yaml], for
 * testing without touching a file system. See [readInstrumentationNode] for semantics.
 */
@ExperimentalApi
fun readInstrumentationNode(
    yaml: String,
    parser: YamlConfigParser = YamlConfigParser(),
): Map<String, Any?>? = extractInstrumentationNode(parser.parse(yaml))

private fun extractInstrumentationNode(document: Any?): Map<String, Any?>? {
    val root = document as? Map<*, *> ?: return null
    val instrumentation = root[INSTRUMENTATION_KEY] as? Map<*, *> ?: return null
    return instrumentation.toStringKeyedMap()
}
