package nonamedb.test.specs.unit.storage.engines

import cats.effect._
import nonamedb.storage.engines.memory.MemoryEngine
import nonamedb.test.specs.unit.UnitSpec

class MemoryEngineSpec extends UnitSpec {
  "A MemoryEngine" should "fail to retrieve missing data" in {
    for {
      engine <- MemoryEngine[IO]()
      result <- engine.get(testKey)
    } yield {
      result should be(None)
    }
  }

  it should "successfully add and retrieve data" in {
    for {
      engine <- MemoryEngine[IO]()
      _ <- engine.put(testKey, testValue)
      result <- engine.get(testKey)
    } yield {
      result should be(Some(testValue))
    }
  }

  it should "successfully update data" in {
    for {
      engine <- MemoryEngine[IO]()
      _ <- engine.put(testKey, testValue)
      initialResult <- engine.get(testKey)
      _ <- engine.put(testKey, updatedTestValue)
      updatedResult <- engine.get(testKey)
    } yield {
      initialResult should be(Some(testValue))
      updatedResult should be(Some(updatedTestValue))
    }
  }

  it should "successfully remove data" in {
    for {
      engine <- MemoryEngine[IO]()
      _ <- engine.put(testKey, testValue)
      initialResult <- engine.get(testKey)
      _ <- engine.put(testKey, Array.emptyByteArray)
      updatedResult <- engine.get(testKey)
    } yield {
      initialResult should be(Some(testValue))
      updatedResult should be(None)
    }
  }

  private val testKey = "some key"
  private val testValue = "some value".getBytes
  private val updatedTestValue = "some updated value".getBytes
}
