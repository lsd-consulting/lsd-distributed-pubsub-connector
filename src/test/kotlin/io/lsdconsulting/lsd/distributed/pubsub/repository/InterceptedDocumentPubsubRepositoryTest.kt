package io.lsdconsulting.lsd.distributed.pubsub.repository

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
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.encodeToByteArray
import kotlinx.serialization.protobuf.ProtoBuf
import org.junit.jupiter.api.Test
import java.util.*

@OptIn(ExperimentalSerializationApi::class)
class InterceptedDocumentPubsubRepositoryTest {
    private val kRandom = KRandom()
    private val publisher = mockk<Publisher>(relaxUnitFun = true)
    private val underTest = InterceptedDocumentPubsubRepository(publisher)

    @Test
    fun `should call publish save is invoked`() {
        val interceptedInteraction: InterceptedInteraction = kRandom.nextObject(InterceptedInteraction::class.java)
        val byteArray = ProtoBuf.encodeToByteArray(interceptedInteraction)
        val pubsubMessage = PubsubMessage.newBuilder().setData(ByteString.copyFrom(byteArray)).build()
        every { publisher.publish(eq(pubsubMessage)) } returns SettableApiFuture.create<String>()
            .apply { set("messageId") }

        underTest.save(interceptedInteraction)

        verify { publisher.publish(eq(pubsubMessage)) }
    }

    @Test
    fun `should not throw exception when exception encountered during publishing`() {
        val interceptedInteraction: InterceptedInteraction = kRandom.nextObject(InterceptedInteraction::class.java)
        val byteArray = ProtoBuf.encodeToByteArray(interceptedInteraction)
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
