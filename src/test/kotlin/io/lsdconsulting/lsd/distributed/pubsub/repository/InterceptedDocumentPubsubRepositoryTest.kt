package io.lsdconsulting.lsd.distributed.pubsub.repository

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.google.api.core.SettableApiFuture
import com.google.cloud.pubsub.v1.Publisher
import com.google.protobuf.ByteString
import com.google.pubsub.v1.PubsubMessage
import io.github.krandom.KRandom
import io.kotest.assertions.throwables.shouldNotThrow
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.lsdconsulting.lsd.distributed.connector.model.InterceptedInteraction
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import java.util.*

class InterceptedDocumentPubsubRepositoryTest {
    private val kRandom = KRandom()
    private val publisher = mockk<Publisher>(relaxUnitFun = true)
    private val objectMapper = ObjectMapper().apply {
        registerModules(KotlinModule.Builder().build(), JavaTimeModule())
        configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
        configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    }
    private val underTest = InterceptedDocumentPubsubRepository(publisher, objectMapper)

    @Test
    fun `should call publish save is invoked`() {
        val interceptedInteraction = kRandom.nextObject(InterceptedInteraction::class.java)
        val byteArray = objectMapper.writeValueAsBytes(interceptedInteraction)
        val pubsubMessage = PubsubMessage.newBuilder().setData(ByteString.copyFrom(byteArray)).build()
        every { publisher.publish(eq(pubsubMessage)) } returns SettableApiFuture.create<String>()
            .apply { set("messageId") }

        underTest.save(interceptedInteraction)

        verify { publisher.publish(eq(pubsubMessage)) }
    }

    @Test
    fun `should not throw exception when exception encountered during publishing`() {
        val interceptedInteraction = kRandom.nextObject(InterceptedInteraction::class.java)
        val byteArray = objectMapper.writeValueAsBytes(interceptedInteraction)
        val pubsubMessage = PubsubMessage.newBuilder().setData(ByteString.copyFrom(byteArray)).build()
        every { publisher.publish(eq(pubsubMessage)) } returns SettableApiFuture.create<String>()
            .apply { setException(RuntimeException("error")) }

        shouldNotThrow<Throwable> {
            underTest.save(interceptedInteraction)
        }

        verify { publisher.publish(eq(pubsubMessage)) }
    }

    @Test
    fun `should throw not implemented error when find by trace ids is invoked`() {
        val id = UUID.randomUUID().toString()

        shouldThrow<NotImplementedError> {
            underTest.findByTraceIds(id)
        }
    }

    @Test
    fun `should return true when is active is invoked`() {
        underTest.isActive() shouldBe true
    }
}
