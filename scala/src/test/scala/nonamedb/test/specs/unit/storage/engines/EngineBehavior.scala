package nonamedb.test.specs.unit.storage.engines

import akka.Done
import nonamedb.storage.Engine
import org.scalatest.{fixture, Matchers}

trait EngineBehavior { this: fixture.AsyncFlatSpec with Matchers =>

  private val testKey = "some key"
  private val testValue = "some value".getBytes
  private val updatedTestValue = "some updated value".getBytes

  def basic(testEngine: Engine) {
    it should "fail to retrieve missing data" in { _ =>
      testEngine.get(testKey).map(_ should be(None))
    }

    it should "successfully add data" in { _ =>
      testEngine.put(testKey, testValue).map(_ should be(Done))
    }

    it should "successfully retrieve data" in { _ =>
      testEngine.get(testKey).map(_ should be(Some(testValue)))
    }

    it should "successfully update data" in { _ =>
      testEngine.put(testKey, updatedTestValue).map(_ should be(Done))
    }

    it should "successfully retrieve updated data" in { _ =>
      testEngine.get(testKey).map(_ should be(Some(updatedTestValue)))
    }

    it should "successfully remove data" in { _ =>
      testEngine.put(testKey, Array.emptyByteArray).map(_ should be(Done))
    }

    it should "fail to retrieve removed data" in { _ =>
      testEngine.get(testKey).map(_ should be(None))
    }
  }
}
