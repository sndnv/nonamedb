package nonamedb.test.specs.unit.storage.engines

import io.kotlintest.*
import io.kotlintest.specs.StringSpec
import nonamedb.storage.Done
import nonamedb.storage.engines.MemoryEngine

class MemoryEngineSpec : StringSpec(){
    init {
        val timeout = 5.seconds
        val testKey = "some key"
        val testValue = "some value".toByteArray()
        val updatedTestValue = "some updated value".toByteArray()
        val testEngine = MemoryEngine()

        "should fail to retrieve missing data" {
            val result = testEngine.get(testKey)
            eventually(timeout) {
                result.getCompleted() shouldBe null
            }
        }

        "should successfully add data" {
            val result = testEngine.put(testKey, testValue)
            eventually(timeout) {
                result.getCompleted() shouldBe Done
            }
        }

        "should successfully retrieve data" {
            val result = testEngine.get(testKey)
            eventually(timeout) {
                result.getCompleted() shouldBe testValue
            }
        }

        "should successfully update data" {
            val result = testEngine.put(testKey, updatedTestValue)
            eventually(timeout) {
                result.getCompleted() shouldBe Done
            }
        }

        "should successfully retrieve updated data" {
            val result = testEngine.get(testKey)
            eventually(timeout) {
                result.getCompleted() shouldBe updatedTestValue
            }
        }

        "should successfully remove data" {
            val result = testEngine.put(testKey, "".toByteArray())
            eventually(timeout) {
                result.getCompleted() shouldBe Done
            }
        }

        "should fail to retrieve removed data" {
            val result = testEngine.get(testKey)
            eventually(timeout) {
                result.getCompleted() shouldBe null
            }
        }
    }
}
