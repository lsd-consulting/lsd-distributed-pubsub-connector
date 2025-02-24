package io.lsdconsulting.lsd.distributed.pubsub.config

import io.lsdconsulting.lsd.distributed.connector.repository.InterceptedDocumentRepository
import io.lsdconsulting.lsd.distributed.pubsub.repository.InterceptedDocumentPubsubRepository
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@ConditionalOnProperty(name = ["lsd.dist.connectionString"])
class PubsubLibraryConfig {
    @Bean
    @Suppress("unused")
    @ConditionalOnExpression("#{'\${lsd.dist.connectionString:}'.startsWith('pubsub')}")
    fun interceptedDocumentRepository(): InterceptedDocumentRepository {
        return InterceptedDocumentPubsubRepository()
    }
}
