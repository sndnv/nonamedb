package nonamedb.test.specs.unit

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpMethods, HttpRequest, StatusCodes}
import akka.util.ByteString
import nonamedb.Service

class ServiceSpec extends UnitSpec {
  "A Service" should "provide an API endpoint" in {
    val service = new Service()

    val expectedEntity = "abcdefg"

    for {
      putResponse <- Http().singleRequest(
        request = HttpRequest(
          method = HttpMethods.PUT,
          uri = "http://localhost:39000/test",
          entity = expectedEntity
        )
      )
      getResponse <- Http().singleRequest(
        request = HttpRequest(
          method = HttpMethods.GET,
          uri = "http://localhost:39000/test"
        )
      )
      actualEntity <- getResponse.entity.dataBytes.runFold(ByteString.empty)(_ concat _)
      _ <- service.stop()
    } yield {
      putResponse.status should be(StatusCodes.OK)
      getResponse.status should be(StatusCodes.OK)

      actualEntity.utf8String should be(expectedEntity)
    }
  }

  private implicit val system: ActorSystem = ActorSystem("ServiceSpec")
}
