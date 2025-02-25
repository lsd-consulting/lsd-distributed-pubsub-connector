package io.lsdconsulting.lsd.distributed.pubsub.repository

import io.github.krandom.KRandom
import io.kotest.assertions.throwables.shouldThrow
import io.lsdconsulting.lsd.distributed.connector.model.InterceptedInteraction
import org.junit.jupiter.api.Test

class InterceptedDocumentPubsubRepositoryTest {
    private val kRandom = KRandom()
    private val underTest = InterceptedDocumentPubsubRepository()

    @Test
    fun `should throw not implemented error when save is invoked`() {
        shouldThrow<NotImplementedError> {
            underTest.save(kRandom.nextObject(InterceptedInteraction::class.java))
        }
    }

    @Test
    fun `should throw not implemented error when find by trace ids is invoked`() {
        shouldThrow<NotImplementedError> {
            underTest.findByTraceIds("")
        }
    }

    @Test
    fun `should throw not implemented error when is active is invoked`() {
        shouldThrow<NotImplementedError> {
            underTest.isActive()
        }
    }
}
