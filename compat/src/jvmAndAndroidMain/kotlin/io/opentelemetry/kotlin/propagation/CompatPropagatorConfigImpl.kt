package io.opentelemetry.kotlin.propagation

import io.opentelemetry.api.baggage.propagation.W3CBaggagePropagator
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator
import io.opentelemetry.context.propagation.TextMapPropagator.composite
import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.aliases.OtelJavaTextMapPropagator
import io.opentelemetry.kotlin.factory.CompatSpanContextFactory
import io.opentelemetry.kotlin.factory.CompatSpanFactory
import io.opentelemetry.kotlin.factory.CompatTraceFlagsFactory
import io.opentelemetry.kotlin.factory.CompatTraceStateFactory
import io.opentelemetry.kotlin.init.B3Format
import io.opentelemetry.kotlin.init.PropagatorConfigDsl

@OptIn(ExperimentalApi::class)
internal class CompatPropagatorConfigImpl : PropagatorConfigDsl {

    private var configured: TextMapPropagator = TextMapPropagatorAdapter(OtelJavaTextMapPropagator.noop())

    override fun composite(vararg propagators: TextMapPropagator): TextMapPropagator {
        val javaPropagators = propagators.map { it.toOtelJavaTextMapPropagator() }
        configured = TextMapPropagatorAdapter(composite(javaPropagators))
        return configured
    }

    override fun w3cBaggage(): TextMapPropagator {
        configured = TextMapPropagatorAdapter(W3CBaggagePropagator.getInstance())
        return configured
    }

    override fun w3cTraceContext(): TextMapPropagator {
        configured = TextMapPropagatorAdapter(W3CTraceContextPropagator.getInstance())
        return configured
    }

    override fun b3(format: B3Format): TextMapPropagator {
        val spanContextFactory = CompatSpanContextFactory()
        val propagator = B3Propagator(
            format = format,
            traceFlagsFactory = CompatTraceFlagsFactory(),
            traceStateFactory = CompatTraceStateFactory(),
            spanContextFactory = spanContextFactory,
            spanFactory = CompatSpanFactory(spanContextFactory),
        )
        configured = propagator
        return propagator
    }

    internal fun buildPropagator(): TextMapPropagator = configured

    private fun TextMapPropagator.toOtelJavaTextMapPropagator(): OtelJavaTextMapPropagator =
        (this as? TextMapPropagatorAdapter)?.impl ?: OtelJavaTextMapPropagatorAdapter(this)
}
