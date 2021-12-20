package nonamedb.tracing

import akka.Done
import io.opentelemetry.api.common.Attributes
import io.opentelemetry.api.trace.Tracer
import io.opentelemetry.exporter.jaeger.JaegerGrpcSpanExporter
import io.opentelemetry.sdk.OpenTelemetrySdk
import io.opentelemetry.sdk.common.CompletableResultCode
import io.opentelemetry.sdk.resources.Resource
import io.opentelemetry.sdk.trace.SdkTracerProvider
import io.opentelemetry.sdk.trace.`export`.SimpleSpanProcessor
import io.opentelemetry.sdk.trace.samplers.{Sampler => OpenTelemetrySampler}
import io.opentelemetry.semconv.resource.attributes.ResourceAttributes

import scala.concurrent.{ExecutionContext, Future, Promise}

class JaegerTracing(
  name: String,
  config: JaegerTracing.Config
)(implicit ec: ExecutionContext)
    extends Tracing {
  import JaegerTracing._

  private val exporter = JaegerGrpcSpanExporter
    .builder()
    .setEndpoint(config.endpoint)
    .build()

  private val tracerProvider = SdkTracerProvider
    .builder()
    .addSpanProcessor(SimpleSpanProcessor.create(exporter))
    .setResource(
      Resource.getDefault.merge(
        Resource.create(Attributes.of(ResourceAttributes.SERVICE_NAME, name))
      )
    )
    .setSampler(config.sampler)
    .build()

  override val tracer: Tracer = OpenTelemetrySdk
    .builder()
    .setTracerProvider(tracerProvider)
    .build()
    .getTracer(name)

  override def shutdown(): Future[Done] =
    for {
      _ <- tracerProvider.shutdown().future
      _ <- exporter.shutdown().future
    } yield {
      Done
    }
}

object JaegerTracing {
  def apply(name: String, config: Config)(implicit ec: ExecutionContext): JaegerTracing =
    new JaegerTracing(name, config)

  final case class Config(
    endpoint: String,
    sampler: OpenTelemetrySampler
  )

  object Config {
    def apply(config: com.typesafe.config.Config): Config =
      Config(
        endpoint = config.getString("endpoint"),
        sampler = Sampler(config = config.getConfig("sampler"))
      )

    object Sampler {
      def apply(config: com.typesafe.config.Config): OpenTelemetrySampler = {
        val overrideParent = config.getBoolean("override-parent")

        val sampler = config.getString("type").trim.toLowerCase match {
          case "on"    => OpenTelemetrySampler.alwaysOn()
          case "off"   => OpenTelemetrySampler.alwaysOff()
          case "ratio" => OpenTelemetrySampler.traceIdRatioBased(config.getDouble("ratio"))
        }

        if (overrideParent) sampler
        else OpenTelemetrySampler.parentBased(sampler)
      }
    }
  }

  implicit class CompletableResultCodeAsFuture(result: CompletableResultCode) {
    def future: Future[Done] = {
      val promise = Promise[Done]()

      result.whenComplete(() => {
        if (result.isSuccess) {
          promise.success(Done)
        } else {
          promise.failure(new RuntimeException("CompletableResultCode failed"))
        }
      })

      promise.future
    }
  }
}
