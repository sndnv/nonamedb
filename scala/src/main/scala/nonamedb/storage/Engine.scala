package nonamedb.storage

import akka.Done
import io.opentelemetry.api.trace.Span

import scala.concurrent.Future

trait Engine {
  def get(key: Key)(implicit span: Span): Future[Option[Value]]
  def put(key: Key, value: Value)(implicit span: Span): Future[Done]
}
