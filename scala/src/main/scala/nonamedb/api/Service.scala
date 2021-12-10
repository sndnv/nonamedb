package nonamedb.api

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.util.ByteString
import nonamedb.storage.Engine

import scala.concurrent.Future

class Service(engine: Engine)(implicit system: ActorSystem) {

  import system.dispatcher

  def start(bindAddress: String, bindPort: Int): Future[Http.ServerBinding] =
    Http().newServerAt(bindAddress, bindPort).bind(routes)

  val routes: Route =
    path(Segment) { id =>
      concat(
        get {
          onComplete(engine.get(id)) { result =>
            result.toOption.flatten match {
              case Some(data) =>
                complete(
                  HttpResponse(
                    entity = HttpEntity(MediaTypes.`application/octet-stream`, data)
                  )
                )
              case None =>
                reject
            }
          }
        },
        (post | put) {
          extractDataBytes { dataStream =>
            complete {
              dataStream
                .runFold(ByteString.empty) {
                  case (acc, current) =>
                    acc ++ current
                }
                .flatMap { data =>
                  engine.put(id, data.toArray)
                }
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
