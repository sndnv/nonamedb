package nonamedb.api

import cats.effect._
import cats.implicits._
import com.comcast.ip4s._
import nonamedb.storage._
import org.http4s._
import org.http4s.dsl.Http4sDsl
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.{Router, Server}

class Api[F[_]: Async](engine: Engine[F]) {
  private object dsl extends Http4sDsl[F]
  import dsl._

  val routes: HttpRoutes[F] = HttpRoutes.of[F] {
    case GET -> Root / id =>
      engine.get(id).flatMap(_.fold(NotFound())(Ok(_)))

    case request @ POST -> Root / id =>
      request.as[Value].flatMap(engine.put(id, _)).flatMap(_ => Ok())

    case request @ PUT -> Root / id =>
      request.as[Value].flatMap(engine.put(id, _)).flatMap(_ => Ok())

    case DELETE -> Root / id =>
      engine.put(key = id, value = Array.emptyByteArray).flatMap(_ => Ok())
  }

  def start(bindAddress: Host, bindPort: Port): Resource[F, Server] =
    EmberServerBuilder
      .default[F]
      .withHost(bindAddress)
      .withPort(bindPort)
      .withHttpApp(Router("/" -> routes).orNotFound)
      .build
}
