package nonamedb.api

import java.util

import scala.concurrent.Future
import scala.jdk.CollectionConverters._

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{Directive, Directive1, Route, RouteResult}
import akka.util.ByteString
import io.opentelemetry.api.common.AttributeKey
import io.opentelemetry.api.trace.{Span, Tracer, StatusCode => SpanStatusCode}
import io.opentelemetry.semconv.trace.attributes.SemanticAttributes
import nonamedb.storage.Engine

class Api(engine: Engine, tracer: Tracer)(implicit system: ActorSystem) {
  import Api._
  import system.dispatcher

  def start(bindAddress: String, bindPort: Int): Future[Http.ServerBinding] =
    Http().newServerAt(bindAddress, bindPort).bind(routes)

  val routes: Route =
    path(Segment) { id =>
      withSpan(tracer, name = "/{id}") { implicit span =>
        concat(
          get {
            onSuccess(engine.get(id)) {
              case Some(data) => complete(HttpEntity(MediaTypes.`application/octet-stream`, data))
              case None       => reject
            }
          },
          (post | put) {
            extractDataBytes { stream =>
              complete {
                stream
                  .runFold(ByteString.empty)(_ concat _)
                  .flatMap(data => engine.put(id, data.toArray))
              }
            }
          },
          delete {
            complete {
              engine.put(id, Array.emptyByteArray)
            }
          }
        )
      }
    }
}

object Api {
  def apply(engine: Engine, tracer: Tracer)(implicit system: ActorSystem): Api =
    new Api(engine, tracer)

  def withSpan(tracer: Tracer, name: String): Directive1[Span] = Directive { inner =>
    (extractMethod & extractRequest & extractClientIP) { (method, request, clientIp) =>
      val span = tracer
        .spanBuilder(name)
        .setAttribute(SemanticAttributes.HTTP_ROUTE, name)
        .setAttribute(SemanticAttributes.HTTP_METHOD, method.value)
        .setAttribute(SemanticAttributes.HTTP_URL, request.uri.toString)
        .setAttribute(SemanticAttributes.HTTP_CLIENT_IP, clientIp.toIP.map(_.ip.toString).getOrElse("none"))
        .startSpan()

      mapRouteResult {
        case result @ RouteResult.Complete(response) =>
          span.setStatus(SpanStatusCode.OK)
          span.setAttribute(SemanticAttributes.HTTP_STATUS_CODE, response.status.intValue())
          span.end()
          result

        case result @ RouteResult.Rejected(rejections) =>
          span.setStatus(SpanStatusCode.ERROR)
          span.setAttribute(Attributes.Rejections, rejections.map(_.toString).asJava)
          span.end()
          result
      }(inner(Tuple1(span)))
    }
  }

  object Attributes {
    val Rejections: AttributeKey[util.List[String]] = AttributeKey.stringArrayKey("nonamedb.rejections")
  }
}
