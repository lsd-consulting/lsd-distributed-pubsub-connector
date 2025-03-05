package io.lsdconsulting.lsd.distributed.pubsub.util

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.read.ListAppender
import com.google.api.core.SettableApiFuture
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldMatch
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import java.util.UUID
import java.util.concurrent.ExecutionException

internal class PubsubPublishCallbackTest {

    @Test
    fun `should log message id on success`() {
        val messageId = UUID.randomUUID().toString()
        val future = SettableApiFuture.create<String>().apply { set(messageId) }
        val listAppender = setUpTestLogger()
        future.addPubsubPublishCallback()

        future.get()

        val iLoggingEvents = listAppender.list
        iLoggingEvents shouldHaveSize 1
        iLoggingEvents[0].level shouldBe Level.INFO
        iLoggingEvents[0].message shouldBe "Published with message id: $messageId"
    }

    @Test
    fun `should log exception id on error`() {
        val future = SettableApiFuture.create<String>().apply { setException(RuntimeException("error")) }
        val listAppender = setUpTestLogger()
        future.addPubsubPublishCallback()

        shouldThrow<ExecutionException> {
            future.get()
        }

        val iLoggingEvents = listAppender.list
        iLoggingEvents shouldHaveSize 1
        iLoggingEvents[0].level shouldBe Level.ERROR
        iLoggingEvents[0].message shouldMatch "Failed to publish.*".toRegex()
    }

    companion object {
        @JvmStatic
        private fun setUpTestLogger(): ListAppender<ILoggingEvent> {
            // get Logback Logger
            val fooLogger: Logger =
                LoggerFactory.getLogger(PubsubPublishCallback::class.java) as Logger
            // create and start a ListAppender
            val listAppender = ListAppender<ILoggingEvent>()
            listAppender.start()
            // add the appender to the logger
            fooLogger.addAppender(listAppender)
            return listAppender
        }
    }
}
