package io.opentelemetry.kotlin.tracing.sampling

import io.opentelemetry.kotlin.attributes.AttributeContainer
import io.opentelemetry.kotlin.context.Context
import io.opentelemetry.kotlin.tracing.SpanKind
import io.opentelemetry.kotlin.tracing.model.SpanLink
import io.opentelemetry.kotlin.tracing.sampling.SamplingResult.Decision

/**
 *
 * This sampler will return the sampling result of the provided {@link #rootSampler}, unless the
 * sampling result contains the sampling decision {@link SamplingDecision#DROP}, in which case, a
 * new sampling result will be returned that is functionally equivalent to the original, except that
 * it contains the sampling decision {@link SamplingDecision#RECORD_ONLY}. This is typically used to
 * enable accurate span-to-metrics processing.
 *
 * @param root The sampler being wrapped; it provides the original sample/drop decision that AlwaysRecord modifies.
 */
internal class AlwaysRecordSampler(private val root: Sampler) : Sampler {

    override val description: String = "AlwaysRecord{${root.description}}"

    override fun shouldSample(
        context: Context,
        traceId: String,
        name: String,
        spanKind: SpanKind,
        attributes: AttributeContainer,
        links: List<SpanLink>,
    ): SamplingResult {
        val result = root.shouldSample(context, traceId, name, spanKind, attributes, links)
        return if (result.decision != Decision.DROP) {
            result
        } else {
            SamplingResultImpl(Decision.RECORD_ONLY, result.attributes, result.traceState)
        }
    }
}
