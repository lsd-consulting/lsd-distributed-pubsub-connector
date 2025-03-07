package io.lsdconsulting.lsd.distributed.pubsub.repository

import com.google.cloud.pubsub.v1.Publisher
import com.google.protobuf.ByteString
import com.google.pubsub.v1.PubsubMessage
import io.lsdconsulting.lsd.distributed.connector.model.InterceptedInteraction
import io.lsdconsulting.lsd.distributed.connector.repository.InterceptedDocumentRepository
import io.lsdconsulting.lsd.distributed.pubsub.util.addPubsubPublishCallback
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.encodeToByteArray
import kotlinx.serialization.protobuf.ProtoBuf

class InterceptedDocumentPubsubRepository(
    private val publisher: Publisher
) : InterceptedDocumentRepository {
    @OptIn(ExperimentalSerializationApi::class)
    override fun save(interceptedInteraction: InterceptedInteraction) {
        val bytes = ProtoBuf.encodeToByteArray(interceptedInteraction)
        val pubsubMessage = PubsubMessage.newBuilder().setData(ByteString.copyFrom(bytes)).build()
        val messageIdFuture = publisher.publish(pubsubMessage)
        messageIdFuture.addPubsubPublishCallback() // add logging callback to track success/failure
    }

    override fun findByTraceIds(vararg traceId: String): List<InterceptedInteraction> {
        throw NotImplementedError("Not implemented for pubsub")
    }

    override fun isActive() = true
}
