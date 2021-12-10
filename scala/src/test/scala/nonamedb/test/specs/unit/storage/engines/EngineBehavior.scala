package nonamedb.test.specs.unit.storage.engines

import akka.Done
import nonamedb.storage.Engine
import nonamedb.test.specs.unit.UnitSpec

trait EngineBehavior {
  this: UnitSpec =>

  private val testKey = "some key"
  private val testValue = "some value".getBytes
  private val updatedTestValue = "some updated value".getBytes

  def basic(testEngine: Engine): Unit = {
    it should "fail to retrieve missing data" in {
      testEngine.get(testKey).map(_ should be(None))
    }

    it should "successfully add data" in {
      testEngine.put(testKey, testValue).map(_ should be(Done))
    }

    it should "successfully retrieve data" in {
      testEngine.get(testKey).map(_ should be(Some(testValue)))
    }

    it should "successfully update data" in {
      testEngine.put(testKey, updatedTestValue).map(_ should be(Done))
    }

    it should "successfully retrieve updated data" in {
      testEngine.get(testKey).map(_ should be(Some(updatedTestValue)))
    }

    it should "successfully remove data" in {
      testEngine.put(testKey, Array.emptyByteArray).map(_ should be(Done))
    }

    it should "fail to retrieve removed data" in {
      testEngine.get(testKey).map(_ should be(None))
    }
  }
}
