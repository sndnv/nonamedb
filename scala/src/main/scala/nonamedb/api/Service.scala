package nonamedb.api

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import akka.util.ByteString
import nonamedb.storage.Engine

import scala.concurrent.{ExecutionContextExecutor, Future}

class Service(engine: Engine)(implicit system: ActorSystem) {
  private implicit val materializer: ActorMaterializer = ActorMaterializer()
  private implicit val executionContext: ExecutionContextExecutor = system.dispatcher

  def start(): Future[Http.ServerBinding] = Http().bindAndHandle(routes, "localhost", 8080)

  val routes: Route =
    path(Segment) { id =>
      get {
        onComplete(engine.get(id)) { result =>
          result.toOption.flatten match {
            case Some(data) => complete(HttpResponse(entity = HttpEntity(MediaTypes.`application/octet-stream`, data)))
            case None       => reject
          }
        }
      } ~
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
        } ~
        delete {
          complete {
            engine.put(id, Array.emptyByteArray)
          }
        }
    }
}
