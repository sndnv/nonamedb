package nonamedb

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

import akka.Done
import akka.actor.ActorSystem
import akka.util.Timeout
import nonamedb.api.Api
import nonamedb.storage.engines.memory.MemoryEngine
import nonamedb.tracing.JaegerTracing
import org.slf4j.{Logger, LoggerFactory}

class Service {

  import Service._

  private val name: String = "nonamedb"

  private implicit val system: ActorSystem = ActorSystem(name = s"$name-${java.util.UUID.randomUUID()}")
  private implicit val ec: ExecutionContext = system.dispatcher

  private implicit val log: Logger = LoggerFactory.getLogger(this.getClass.getName)

  private val config = system.settings.config.getConfig(name)

  private val serviceConfig = Config(config.getConfig("service"))
  private val tracingConfig = JaegerTracing.Config(config.getConfig("tracing"))

  private implicit val timeout: Timeout = serviceConfig.timeout

  private val tracing = JaegerTracing(name, tracingConfig)

  private val engine = MemoryEngine(tracing.tracer)

  private val api = Api(engine, tracing.tracer)

  log.info(
    s"""
       |Config(
       |  service:
       |    interface: ${serviceConfig.interface}
       |    port:      ${serviceConfig.port.toString}
       |    timeout:   ${serviceConfig.timeout.duration.toMillis.toString} ms
       |  tracing:
       |    endpoint: ${tracingConfig.endpoint}
       |    sampler:  ${tracingConfig.sampler.toString}
       |)
       """.stripMargin
  )

  private val binding = api.start(bindAddress = serviceConfig.interface, bindPort = serviceConfig.port)

  def stop(): Future[Done] = {
    log.info("Service stopping...")

    for {
      binding <- binding
      _ <- binding.unbind()
      _ <- tracing.shutdown()
    } yield {
      Done
    }
  }

  sys.addShutdownHook { stop() }
}

object Service {
  final case class Config(
    interface: String,
    port: Int,
    timeout: Timeout,
  )

  object Config {
    def apply(config: com.typesafe.config.Config): Config =
      Config(
        interface = config.getString("interface"),
        port = config.getInt("port"),
        timeout = config.getDuration("timeout").toMillis.millis
      )
  }
}
