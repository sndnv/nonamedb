package nonamedb.test.specs.unit.api

import akka.http.javadsl.model.RequestEntity
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.util.{ByteString, Timeout}
import nonamedb.api.Service
import nonamedb.storage.Value
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

  private val testKey = "some-key"
  private val testValue = "some value".getBytes
  private val updatedTestValue = "some updated value".getBytes

  "A Service" should "reject invalid requests" in { _ =>
    Get() ~> testService.routes ~> check {
      handled should be(false)
    }

    Get("/missing-data") ~> testService.routes ~> check {
      handled should be(false)
    }
  }

  it should "successfully add data" in { _ =>
    Post(s"/$testKey").withEntity(testValue) ~> testService.routes ~> check {
      status should be(StatusCodes.OK)
    }
  }

  it should "successfully retrieve new data" in { _ =>
    Get(s"/$testKey") ~> testService.routes ~> check {
      status should be(StatusCodes.OK)
      responseAs[Value] should be(testValue)
    }
  }

  it should "successfully update data" in { _ =>
    Put(s"/$testKey").withEntity(updatedTestValue) ~> testService.routes ~> check {
      status should be(StatusCodes.OK)
    }
  }

  it should "successfully retrieve updated data" in { _ =>
    Get(s"/$testKey") ~> testService.routes ~> check {
      status should be(StatusCodes.OK)
      responseAs[Value] should be(updatedTestValue)
    }
  }

  it should "successfully delete data" in { _ =>
    Delete(s"/$testKey") ~> testService.routes ~> check {
      status should be(StatusCodes.OK)
    }
  }

  it should "fail to retrieve delete data" in { _ =>
    Get(s"/$testKey") ~> testService.routes ~> check {
      handled should be(false)
    }
  }
}
