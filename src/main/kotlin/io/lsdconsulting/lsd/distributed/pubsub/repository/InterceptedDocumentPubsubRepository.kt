package io.lsdconsulting.lsd.distributed.pubsub.repository

import io.lsdconsulting.lsd.distributed.connector.model.InterceptedInteraction
import io.lsdconsulting.lsd.distributed.connector.repository.InterceptedDocumentRepository

class InterceptedDocumentPubsubRepository : InterceptedDocumentRepository {
    override fun save(interceptedInteraction: InterceptedInteraction) {
        TODO("To be implemented")
    }

    override fun findByTraceIds(vararg traceId: String): List<InterceptedInteraction> {
        TODO("To be implemented")
    }

    override fun isActive() = TODO("To be implemented")
}
