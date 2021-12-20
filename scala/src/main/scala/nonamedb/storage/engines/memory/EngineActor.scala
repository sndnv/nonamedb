package nonamedb.storage.engines.memory

import akka.Done
import akka.actor.{Actor, ActorLogging, Props}
import io.opentelemetry.api.trace.{Span, Tracer}
import io.opentelemetry.context.Context
import io.opentelemetry.semconv.trace.attributes.SemanticAttributes
import nonamedb.storage.{Key, Value}

class EngineActor(tracer: Tracer) extends Actor with ActorLogging {
  import EngineActor._

  private def process(store: Map[Key, Value]): Receive = {
    case Put(key, value, parentSpan) =>
      val span = tracer
        .spanBuilder("put_data")
        .setParent(Context.current().`with`(parentSpan))
        .setAttribute(SemanticAttributes.DB_NAME, Attributes.DB.Name)
        .setAttribute(SemanticAttributes.DB_USER, Attributes.DB.User)
        .startSpan()

      val updatedStore: Map[Key, Value] =
        if (value.isEmpty) {
          log.debug(
            "[PUT] Removing value with key [{}] (trace={})",
            key,
            span.getSpanContext.getTraceId
          )

          span.setAttribute(SemanticAttributes.DB_OPERATION, Attributes.DB.Operations.Delete)

          store - key
        } else {
          log.debug(
            "[PUT] {} value with key [{}] (trace={})",
            if (store.contains(key)) "Updating" else "Adding",
            key,
            span.getSpanContext.getTraceId
          )

          span
            .setAttribute(SemanticAttributes.DB_OPERATION, Attributes.DB.Operations.Upsert)
            .setAttribute(SemanticAttributes.MESSAGE_COMPRESSED_SIZE, value.length)
            .setAttribute(SemanticAttributes.MESSAGE_UNCOMPRESSED_SIZE, value.length)

          store + (key -> value)
        }

      span.end()
      sender() ! Done
      context.become(process(updatedStore))

    case Get(key, parentSpan) =>
      val span = tracer
        .spanBuilder("get_data")
        .setParent(Context.current().`with`(parentSpan))
        .setAttribute(SemanticAttributes.DB_NAME, Attributes.DB.Name)
        .setAttribute(SemanticAttributes.DB_USER, Attributes.DB.User)
        .startSpan()

      val result: Option[Value] = store.get(key)

      result match {
        case Some(value) =>
          log.debug(
            "[GET] Value with key [{}] found (trace={})",
            key,
            span.getSpanContext.getTraceId
          )

          span
            .setAttribute(SemanticAttributes.MESSAGE_COMPRESSED_SIZE, value.length)
            .setAttribute(SemanticAttributes.MESSAGE_UNCOMPRESSED_SIZE, value.length)
            .end()

        case None =>
          log.debug(
            "[GET] Value with key [{}] not found (trace={})",
            key,
            span.getSpanContext.getTraceId
          )

          span.end()
      }

      sender() ! result
  }

  override def receive: Receive = process(Map.empty)
}

object EngineActor {
  final case class Put(key: Key, value: Value, parentSpan: Span)
  final case class Get(key: Key, parentSpan: Span)

  def props(tracer: Tracer): Props = Props(new EngineActor(tracer))

  object Attributes {
    object DB {
      val Name: String = "nonamedb.memory"
      val User: String = "none"

      object Operations {
        val Upsert: String = "upsert"
        val Delete: String = "delete"
      }
    }
  }
}
