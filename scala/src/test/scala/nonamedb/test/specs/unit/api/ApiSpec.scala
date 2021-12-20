package nonamedb.test.specs.unit.api

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.util.Timeout
import io.opentelemetry.api.trace.TracerProvider
import nonamedb.api.Api
import nonamedb.storage.Value
import nonamedb.storage.engines.memory.MemoryEngine
import nonamedb.test.specs.unit.UnitSpec

import scala.concurrent.duration._

class ApiSpec extends UnitSpec with ScalatestRouteTest {
  "An API" should "reject invalid requests" in {
    Get() ~> testService.routes ~> check {
      handled should be(false)
    }

    Get("/missing-data") ~> testService.routes ~> check {
      handled should be(false)
    }
  }

  it should "successfully add data" in {
    Post(s"/$testKey").withEntity(testValue) ~> testService.routes ~> check {
      status should be(StatusCodes.OK)
    }
  }

  it should "successfully retrieve new data" in {
    Get(s"/$testKey") ~> testService.routes ~> check {
      status should be(StatusCodes.OK)
      responseAs[Value] should be(testValue)
    }
  }

  it should "successfully update data" in {
    Put(s"/$testKey").withEntity(updatedTestValue) ~> testService.routes ~> check {
      status should be(StatusCodes.OK)
    }
  }

  it should "successfully retrieve updated data" in {
    Get(s"/$testKey") ~> testService.routes ~> check {
      status should be(StatusCodes.OK)
      responseAs[Value] should be(updatedTestValue)
    }
  }

  it should "successfully delete data" in {
    Delete(s"/$testKey") ~> testService.routes ~> check {
      status should be(StatusCodes.OK)
    }
  }

  it should "fail to retrieve delete data" in {
    Get(s"/$testKey") ~> testService.routes ~> check {
      handled should be(false)
    }
  }

  private implicit val timeout: Timeout = 3.seconds
  private val tracer = TracerProvider.noop().get("ApiSpec")
  private val testEngine = new MemoryEngine(tracer)
  private val testService = new Api(testEngine, tracer)

  private val testKey = "some-key"
  private val testValue = "some value".getBytes
  private val updatedTestValue = "some updated value".getBytes
}
