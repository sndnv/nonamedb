package nonamedb.test.specs.unit.api

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.util.Timeout
import nonamedb.api.Service
import nonamedb.storage.engines.memory.MemoryEngine
import nonamedb.test.specs.unit.UnitSpec
import org.scalatest.Outcome

import scala.concurrent.duration._

class ServiceSpec extends UnitSpec with ScalatestRouteTest {

  case class FixtureParam()

  def withFixture(test: OneArgTest): Outcome =
    withFixture(test.toNoArgTest(FixtureParam()))

  private implicit val timeout: Timeout = 3.seconds
  private val testEngine = new MemoryEngine()
  private val testService = new Service(testEngine)

  "A Service" should "reject invalid requests" in { _ =>
    Get() ~> testService.routes ~> check {
      fail("Not Implemented", new NotImplementedError())
    }

    Get("/missing-data") ~> testService.routes ~> check {
      fail("Not Implemented", new NotImplementedError())
    }
  }

  it should "successfully add data" in { _ =>
    fail("Not Implemented", new NotImplementedError())
  }

  it should "successfully retrieve new data" in { _ =>
    fail("Not Implemented", new NotImplementedError())
  }

  it should "successfully update data" in { _ =>
    fail("Not Implemented", new NotImplementedError())
  }

  it should "successfully retrieve updated data" in { _ =>
    fail("Not Implemented", new NotImplementedError())
  }

  it should "successfully delete data" in { _ =>
    fail("Not Implemented", new NotImplementedError())
  }

  it should "fail to retrieve delete data" in { _ =>
    fail("Not Implemented", new NotImplementedError())
  }
}
