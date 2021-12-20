package nonamedb.storage.engines.memory

import akka.Done
import akka.actor.{ActorRef, ActorSystem}
import akka.pattern.ask
import akka.util.Timeout
import io.opentelemetry.api.trace.{Span, Tracer}
import nonamedb.storage.engines.memory.EngineActor.{Get, Put}
import nonamedb.storage.{Engine, Key, Value}

import scala.concurrent.Future

class MemoryEngine(tracer: Tracer)(implicit system: ActorSystem, timeout: Timeout) extends Engine {
  private val store: ActorRef = system.actorOf(EngineActor.props(tracer))

  override def get(key: Key)(implicit span: Span): Future[Option[Value]] =
    (store ? Get(key, span)).mapTo[Option[Value]]

  override def put(key: Key, value: Value)(implicit span: Span): Future[Done] =
    (store ? Put(key, value, span)).mapTo[Done]
}

object MemoryEngine {
  def apply(tracer: Tracer)(implicit system: ActorSystem, timeout: Timeout): MemoryEngine =
    new MemoryEngine(tracer)
}
