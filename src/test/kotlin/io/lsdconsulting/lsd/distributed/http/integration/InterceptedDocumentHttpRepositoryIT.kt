package io.lsdconsulting.lsd.distributed.http.integration

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.*
import com.lsdconsulting.generatorui.controller.LsdControllerStub
import io.lsdconsulting.lsd.distributed.connector.model.InteractionType
import io.lsdconsulting.lsd.distributed.connector.model.InterceptedInteraction
import io.lsdconsulting.lsd.distributed.connector.repository.InterceptedDocumentRepository
import io.lsdconsulting.lsd.distributed.http.integration.testapp.TestApplication
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment
import org.springframework.test.context.ActiveProfiles
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT, classes = [TestApplication::class])
class InterceptedDocumentHttpRepositoryIT {
    @Autowired
    private lateinit var underTest: InterceptedDocumentRepository

    private val lsdControllerStub = LsdControllerStub(objectMapper())

    @BeforeEach
    fun setup() {
        reset()
    }

    @Test
    fun shouldCallCorrectStorageEndpointWhenSavingInteraction() {
        val interceptedInteraction = interceptedInteraction()
        lsdControllerStub.store(interceptedInteraction)

        underTest.save(interceptedInteraction)

        lsdControllerStub.verifyStore(interceptedInteraction)
    }

    @Test
    fun shouldHandleUnavailableStorage() {
        val interceptedInteraction = interceptedInteraction()
        lsdControllerStub.store(404, "Not found")

        underTest.save(interceptedInteraction)

        lsdControllerStub.verifyStore(interceptedInteraction)
    }

    @Test
    fun shouldHandleTimeout() {
        val interceptedInteraction = interceptedInteraction()
        stubFor(post("/lsd").willReturn(aResponse().withFixedDelay(5000)))

        underTest.save(interceptedInteraction)

        lsdControllerStub.verifyStore(interceptedInteraction)
    }

    @Test
    fun shouldCallCorrectStorageEndpointWhenRetrievingInteractionsByTraceId() {
        val interceptedInteraction = interceptedInteraction()
        lsdControllerStub.findByTraceIds(mutableListOf(interceptedInteraction), listOf( interceptedInteraction.traceId))

        underTest.findByTraceIds(interceptedInteraction.traceId)

        lsdControllerStub.verifyFindByTraceIds(listOf( interceptedInteraction.traceId))
    }

    private fun objectMapper(): ObjectMapper {
        val mapper = ObjectMapper()
        mapper.registerModule(JavaTimeModule())
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        return mapper
    }

    private fun interceptedInteraction(): InterceptedInteraction =
        InterceptedInteraction(
            elapsedTime = 20L,
            httpStatus = "OK",
            serviceName = "serviceName",
            target = "target",
            path = "/path",
            httpMethod = "GET",
            body = "body",
            interactionType = InteractionType.REQUEST,
            traceId = "traceId",
            createdAt = ZonedDateTime.ofInstant(Instant.ofEpochSecond(0), ZoneId.of("UTC")))

    companion object {
        private val wireMockServer = WireMockServer(8070)

        @BeforeAll
        @JvmStatic
        internal fun beforeAll() {
            configureFor(8070)
            wireMockServer.start()
        }

        @AfterAll
        @JvmStatic
        internal fun afterAll() {
            wireMockServer.stop()
        }
    }
}
