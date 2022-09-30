package nonamedb.test.specs.unit.api

import cats.effect.IO
import nonamedb.api.Api
import nonamedb.storage.Value
import nonamedb.storage.engines.memory.MemoryEngine
import nonamedb.test.specs.unit.UnitSpec
import org.http4s.Method._
import org.http4s._
import org.http4s.implicits._

class ApiSpec extends UnitSpec {
  "An API" should "reject invalid requests" in {
    for {
      service <- createService()
      invalid <- service.routes.orNotFound.run(Request(GET))
      missing <- service.routes.orNotFound.run(Request(GET, uri = uri"/missing-data"))
    } yield {
      invalid.status should be(Status.NotFound)
      missing.status should be(Status.NotFound)
    }
  }

  it should "successfully add and retrieve data" in {
    for {
      service <- createService()
      addedFirst <- service.routes.orNotFound.run(Request(POST, uri = uri"/some-key-1").withEntity(testValue1))
      addedSecond <- service.routes.orNotFound.run(Request(PUT, uri = uri"/some-key-2").withEntity(testValue2))
      retrievedFirst <- service.routes.orNotFound.run(Request(GET, uri = uri"/some-key-1")).flatMap(_.as[Value])
      retrievedSecond <- service.routes.orNotFound.run(Request(GET, uri = uri"/some-key-2")).flatMap(_.as[Value])
    } yield {
      addedFirst.status should be(Status.Ok)
      addedSecond.status should be(Status.Ok)

      retrievedFirst should be(testValue1)
      retrievedSecond should be(testValue2)
    }
  }

  it should "successfully update data" in {
    for {
      service <- createService()
      addedResponse <- service.routes.orNotFound.run(Request(POST, uri = uri"/some-key-1").withEntity(testValue1))
      initial <- service.routes.orNotFound.run(Request(GET, uri = uri"/some-key-1")).flatMap(_.as[Value])
      updatedResponse <- service.routes.orNotFound.run(Request(PUT, uri = uri"/some-key-1").withEntity(updatedTestValue))
      updated <- service.routes.orNotFound.run(Request(GET, uri = uri"/some-key-1")).flatMap(_.as[Value])
    } yield {
      addedResponse.status should be(Status.Ok)
      updatedResponse.status should be(Status.Ok)

      initial should be(testValue1)
      updated should be(updatedTestValue)
    }
  }

  it should "successfully delete data" in {
    for {
      service <- createService()
      addedResponse <- service.routes.orNotFound.run(Request(POST, uri = uri"/some-key-1").withEntity(testValue1))
      initial <- service.routes.orNotFound.run(Request(GET, uri = uri"/some-key-1")).flatMap(_.as[Value])
      deletedResponse <- service.routes.orNotFound.run(Request(DELETE, uri = uri"/some-key-1"))
      deleted <- service.routes.orNotFound.run(Request(GET, uri = uri"/some-key-1"))
    } yield {
      addedResponse.status should be(Status.Ok)
      deletedResponse.status should be(Status.Ok)

      initial should be(testValue1)
      deleted.status should be(Status.NotFound)
    }
  }

  private def createService(): IO[Api[IO]] =
    MemoryEngine[IO]().map(new Api(_))

  private val testValue1 = "some value 1".getBytes
  private val testValue2 = "some value 2".getBytes
  private val updatedTestValue = "some updated value".getBytes
}
