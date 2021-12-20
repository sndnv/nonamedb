package nonamedb.test.specs.unit.storage.engines

import akka.actor.ActorSystem
import akka.util.Timeout
import io.opentelemetry.api.trace.TracerProvider
import nonamedb.storage.engines.memory.MemoryEngine
import nonamedb.test.specs.unit.UnitSpec

import scala.concurrent.duration._

class MemoryEngineSpec extends UnitSpec with EngineBehavior {
  case class FixtureParam()

  private implicit val timeout: Timeout = 3.seconds
  private implicit val system: ActorSystem = ActorSystem("MemoryEngineSpec")

  private val testEngine = new MemoryEngine(TracerProvider.noop().get("MemoryEngineSpec"))

  "A MemoryEngine" should behave like basic(testEngine)
}
