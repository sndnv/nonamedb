package nonamedb.storage.engines

import kotlinx.coroutines.experimental.CompletableDeferred
import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.channels.actor
import kotlinx.coroutines.experimental.launch
import mu.KotlinLogging
import nonamedb.storage.Done
import nonamedb.storage.Engine
import java.util.*

class MemoryEngine : Engine {
    private val store = createActor()
    private val logger = KotlinLogging.logger {}

    override fun get(key: String): Deferred<ByteArray?> {
        val response = CompletableDeferred<ByteArray?>()
        launch { store.send(Get(key, response)) }
        return response
    }

    override fun put(key: String, value: ByteArray): Deferred<Done> {
        val response = CompletableDeferred<Done>()
        launch { store.send(Put(key, value, response)) }
        return response
    }

    private fun createActor() = actor<Message> {
        var store: Map<String, ByteArray> = emptyMap()
        for (message in channel) {
            when (message) {
                is Get -> {
                    val result = store[message.key]
                    logger.debug {
                        "[GET] Value with key [${message.key}] ${if(result != null) "found" else "not found"}"
                    }

                    message.response.complete(result)
                }

                is Put -> {
                    if (message.value.isEmpty()) {
                        logger.debug { "[PUT] Removing value with key [${message.key}]" }
                        store -= message.key
                    } else {
                        logger.debug {
                            val operation = if (store.containsKey(message.key)) "Updating" else "Adding"
                            "[PUT] $operation value with key [${message.key}]"
                        }

                        store += Pair(message.key, message.value)
                    }

                    message.response.complete(Done)
                }
            }
        }
    }
}


private sealed class Message

private data class Get(val key: String, val response: CompletableDeferred<ByteArray?>) : Message()

private data class Put(val key: String, val value: ByteArray, val response: CompletableDeferred<Done>) : Message() {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Put

        if (key != other.key) return false
        if (!Arrays.equals(value, other.value)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = key.hashCode()
        result = 31 * result + Arrays.hashCode(value)
        return result
    }
}
