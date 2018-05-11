package nonamedb.test.specs.unit.storage.engines

import akka.actor.ActorSystem
import akka.util.Timeout
import nonamedb.storage.engines.memory.MemoryEngine
import nonamedb.test.specs.unit.AsyncUnitSpec
import org.scalatest.FutureOutcome

import scala.concurrent.duration._

class MemoryEngineSpec extends AsyncUnitSpec with EngineBehavior {
  case class FixtureParam()

  def withFixture(test: OneArgAsyncTest): FutureOutcome =
    withFixture(test.toNoArgAsyncTest(FixtureParam()))

  private implicit val timeout: Timeout = 3.seconds
  private implicit val system: ActorSystem = ActorSystem("MemoryEngineSpec")

  private val testEngine = new MemoryEngine()

  "A MemoryEngine" should behave like basic(testEngine)
}
