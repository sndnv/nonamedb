package nonamedb.test.specs.unit

import scala.concurrent.duration._

import cats.effect.{IO, Spawn}
import nonamedb.Service
import org.http4s.{Request, Status}
import org.http4s.dsl.io._
import org.http4s.ember.client._
import org.http4s.implicits._

class ServiceSpec extends UnitSpec {
  "A Service" should "provide an API endpoint" in {
    val service = new Service()

    val expectedEntity = "abcdefg"

    EmberClientBuilder.default[IO].build.use { client =>
      val uri = uri"http://localhost:39000/test"

      for {
        _ <- Spawn[IO].start(service.run(args = List.empty))
        _ <- IO.sleep(1.second)
        putResponse <- client.status(Request[IO](method = POST, uri = uri).withEntity(expectedEntity))
        getResponse <- client.expect[String](Request[IO](method = GET, uri = uri))
      } yield {
        putResponse should be(Status.Ok)
        getResponse should be(expectedEntity)
      }
    }
  }
}
