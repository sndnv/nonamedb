package nonamedb.test.specs.unit.api

import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.http.HttpStatus
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.test.test

@RunWith(SpringRunner::class)
@SpringBootTest(webEnvironment = RANDOM_PORT)
class ServiceSpec {
    private val testKey = "some-key"
    private val testValue = "some value".toByteArray()
    private val updatedTestValue = "some updated value".toByteArray()

    @LocalServerPort
    var port: Int? = null

    lateinit var client: WebClient

    @Before
    fun setup() {
        client = WebClient.create("http://localhost:$port")
    }

    @Test
    fun shouldRejectInvalidRequests() {
        client.get().uri("/")
                .exchange()
                .test()
                .expectNextMatches { it.statusCode() == HttpStatus.NOT_FOUND }
                .verifyComplete()

        client.get().uri("/missing-data")
                .exchange()
                .test()
                .expectNextMatches { it.statusCode() == HttpStatus.NOT_FOUND }
                .verifyComplete()
    }

    @Test
    fun shouldSuccessfullyRetrieveData() {
        client.post().uri("/$testKey").body(BodyInserters.fromObject(testValue))
                .exchange()
                .test()
                .expectNextMatches { it.statusCode() == HttpStatus.OK }
                .verifyComplete()

        client.get().uri("/$testKey")
                .retrieve()
                .bodyToMono<ByteArray>()
                .test()
                .expectNextMatches { it!!.contentEquals(testValue) }
                .verifyComplete()
    }

    @Test
    fun shouldSuccessfullyRetrieveUpdatedData() {
        client.put().uri("/$testKey").body(BodyInserters.fromObject(updatedTestValue))
                .exchange()
                .test()
                .expectNextMatches { it.statusCode() == HttpStatus.OK }
                .verifyComplete()

        client.get().uri("/$testKey")
                .retrieve()
                .bodyToMono<ByteArray>()
                .test()
                .expectNextMatches { it!!.contentEquals(updatedTestValue) }
                .verifyComplete()
    }

    @Test
    fun shouldFailToRetrieveDeletedData() {
        client.delete().uri("/$testKey")
                .exchange()
                .test()
                .expectNextMatches { it.statusCode() == HttpStatus.OK }
                .verifyComplete()

        client.get().uri("/$testKey")
                .exchange()
                .test()
                .expectNextMatches { it.statusCode() == HttpStatus.NOT_FOUND }
                .verifyComplete()
    }
}
