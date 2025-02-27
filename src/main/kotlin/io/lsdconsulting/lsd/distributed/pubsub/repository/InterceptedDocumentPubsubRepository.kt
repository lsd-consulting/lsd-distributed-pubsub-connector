package io.lsdconsulting.lsd.distributed.pubsub.repository

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.cloud.pubsub.v1.Publisher
import com.google.protobuf.ByteString
import com.google.pubsub.v1.PubsubMessage
import io.lsdconsulting.lsd.distributed.connector.model.InterceptedInteraction
import io.lsdconsulting.lsd.distributed.connector.repository.InterceptedDocumentRepository
import io.lsdconsulting.lsd.distributed.pubsub.util.addPubsubPublishCallback

class InterceptedDocumentPubsubRepository(
    private val publisher: Publisher,
    private val objectMapper: ObjectMapper
) : InterceptedDocumentRepository {
    override fun save(interceptedInteraction: InterceptedInteraction) {
        val bytes = objectMapper.writeValueAsBytes(interceptedInteraction)
        val pubsubMessage = PubsubMessage.newBuilder().setData(ByteString.copyFrom(bytes)).build()
        val messageIdFuture = publisher.publish(pubsubMessage)
        messageIdFuture.addPubsubPublishCallback() // add logging callback to track success/failure
    }

    override fun findByTraceIds(vararg traceId: String): List<InterceptedInteraction> {
        throw NotImplementedError("Not implemented for pubsub")
    }

    override fun isActive() = true
}
