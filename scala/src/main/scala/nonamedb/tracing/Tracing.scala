package nonamedb.tracing

import scala.concurrent.Future

import akka.Done
import io.opentelemetry.api.trace.Tracer

trait Tracing {
  def tracer: Tracer
  def shutdown(): Future[Done]
}
