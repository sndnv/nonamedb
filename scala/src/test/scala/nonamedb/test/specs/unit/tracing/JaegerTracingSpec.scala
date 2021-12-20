package nonamedb.test.specs.unit.tracing

import akka.Done
import com.typesafe.config.ConfigFactory
import io.opentelemetry.sdk.common.CompletableResultCode
import io.opentelemetry.sdk.trace.samplers.Sampler
import nonamedb.test.specs.unit.UnitSpec
import nonamedb.tracing.JaegerTracing

class JaegerTracingSpec extends UnitSpec {
  "A JaegerTracing" should "load its config" in {
    val expectedConfig = JaegerTracing.Config(
      endpoint = "http://localhost:14250",
      sampler = Sampler.alwaysOn()
    )

    val config = ConfigFactory.load().getConfig("nonamedb.test")

    JaegerTracing.Config(config = config.getConfig("tracing-on")) should be(
      expectedConfig
    )

    JaegerTracing.Config(config = config.getConfig("tracing-off")) should be(
      expectedConfig.copy(sampler = Sampler.alwaysOff())
    )

    JaegerTracing.Config(config = config.getConfig("tracing-ratio")) should be(
      expectedConfig.copy(sampler = Sampler.traceIdRatioBased(0.1))
    )

    JaegerTracing.Config(config = config.getConfig("tracing-parent-on")) should be(
      expectedConfig.copy(sampler = Sampler.parentBased(Sampler.alwaysOn()))
    )
  }

  it should "support converting CompletableResultCodes to Scala futures" in {
    import JaegerTracing.CompletableResultCodeAsFuture

    for {
      successfulResult <- new CompletableResultCode().succeed().future
      failedResult <- new CompletableResultCode().fail().future.failed
    } yield {
      successfulResult should be(Done)
      failedResult shouldBe an[RuntimeException]
      failedResult.getMessage should be("CompletableResultCode failed")
    }
  }
}
