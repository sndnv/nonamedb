package nonamedb.storage

import kotlinx.coroutines.experimental.Deferred

interface Engine {
    fun get(key: String): Deferred<ByteArray?>
    fun put(key: String, value: ByteArray): Deferred<Done>
}

object Done
