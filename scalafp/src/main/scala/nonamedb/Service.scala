package nonamedb

import cats.effect.{ExitCode, IO, IOApp}
import com.comcast.ip4s._
import nonamedb.api.Api
import nonamedb.storage.engines.memory.MemoryEngine
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger
import pureconfig._
import pureconfig.generic.auto._

class Service extends IOApp {
  private val log: Logger[IO] = Slf4jLogger.getLogger[IO]

  override def run(args: List[String]): IO[ExitCode] =
    for {
      config <- IO { ConfigSource.default.at("nonamedb.service").loadOrThrow[Service.Config] }
      engine <- MemoryEngine[IO]()
      _ <- log.info(
        s"""
             |Config(
             |  service:
             |    interface: ${config.interface}
             |    port:      ${config.port.toString}
             |)""".stripMargin
      )
      exitCode <- new Api(engine)
        .start(
          bindAddress = Host.fromString(config.interface).getOrElse(host"localhost"),
          bindPort = Port.fromInt(config.port).getOrElse(port"8080")
        )
        .use(_ => IO.never)
        .as(ExitCode.Success)
    } yield {
      exitCode
    }
}

object Service {
  final case class Config(
    interface: String,
    port: Int
  )
}
