package nonamedb.api

import kotlinx.coroutines.experimental.reactive.awaitSingle
import kotlinx.coroutines.experimental.reactor.mono
import nonamedb.storage.Done
import nonamedb.storage.Engine
import nonamedb.storage.engines.MemoryEngine
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.server.RouterFunction
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.ServerResponse.notFound
import org.springframework.web.reactive.function.server.ServerResponse.ok
import org.springframework.web.reactive.function.server.router
import reactor.core.publisher.Mono

@Configuration
class RoutingConfiguration {
    @Bean
    fun routerFunction(handler: ServiceHandler): RouterFunction<ServerResponse> = router {
        GET("/{key}") { request ->
            val key = request.pathVariable("key")
            handler.get(key).flatMap { result ->
                ok().body(BodyInserters.fromObject(result))
            }.switchIfEmpty(notFound().build())
        }

        PUT("/{key}") { request ->
            val key = request.pathVariable("key")
            val monoValue = request.bodyToMono(ByteArray::class.java)
            handler.put(key, monoValue).flatMap { _ -> ok().build() }
        }

        POST("/{key}") { request ->
            val key = request.pathVariable("key")
            val monoValue = request.bodyToMono(ByteArray::class.java)
            handler.put(key, monoValue).flatMap { _ -> ok().build() }
        }

        DELETE("/{key}") { request ->
            val key = request.pathVariable("key")
            handler.delete(key).flatMap { _ -> ok().build() }
        }
    }
}

@Component
class ServiceHandler {
    @Autowired
    lateinit var engine: Engine

    fun get(key: String): Mono<ByteArray> = mono {
        engine.get(key).await()
    }

    fun put(key: String, value: Mono<ByteArray>): Mono<Done> = mono {
        engine.put(key, value.awaitSingle()).await()
    }

    fun delete(key: String): Mono<Done> = mono {
        engine.put(key, "".toByteArray()).await()
    }
}

@Component
class PlaceholderEngine : Engine {
    private val engine = MemoryEngine()
    override fun get(key: String) = engine.get(key)
    override fun put(key: String, value: ByteArray) = engine.put(key, value)
}
