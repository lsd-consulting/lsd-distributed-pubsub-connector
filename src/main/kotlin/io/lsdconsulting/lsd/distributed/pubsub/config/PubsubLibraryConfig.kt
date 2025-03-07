package io.lsdconsulting.lsd.distributed.pubsub.config

import com.google.api.gax.core.CredentialsProvider
import com.google.api.gax.rpc.TransportChannelProvider
import com.google.cloud.pubsub.v1.Publisher
import com.google.cloud.spring.core.GcpProjectIdProvider
import com.google.pubsub.v1.TopicName
import io.lsdconsulting.lsd.distributed.connector.repository.InterceptedDocumentRepository
import io.lsdconsulting.lsd.distributed.pubsub.repository.InterceptedDocumentPubsubRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

private const val PUBSUB_TOPIC_REGEX = "(?<prefix>pubsub://)(?<topicName>[A-Za-z0-9-_.~+%]+)"

@Configuration
@ConditionalOnProperty(name = ["lsd.dist.connectionString"])
class PubsubLibraryConfig {
    @Bean
    @Suppress("unused")
    @ConditionalOnExpression("#{'\${lsd.dist.connectionString:}'.startsWith('pubsub')}")
    fun interceptedDocumentRepository(
        publisher: Publisher
    ): InterceptedDocumentRepository = InterceptedDocumentPubsubRepository(publisher)

    @Bean(destroyMethod = "shutdown")
    @ConditionalOnExpression("#{'\${lsd.dist.connectionString:}'.startsWith('pubsub')}")
    fun publisher(
        topicName: TopicName,
        channelProvider: TransportChannelProvider? = null,
        credentialsProvider: CredentialsProvider? = null
    ): Publisher {
        val publisherBuilder = Publisher.newBuilder(topicName)
        channelProvider?.let { publisherBuilder.setChannelProvider(it) }
        credentialsProvider?.let { publisherBuilder.setCredentialsProvider(it) }
        return publisherBuilder.build()
    }

    @Bean
    @ConditionalOnExpression("#{'\${lsd.dist.connectionString:}'.startsWith('pubsub')}")
    fun topicName(
        projectIdProvider: GcpProjectIdProvider,
        @Value("\${lsd.dist.connectionString}") connectionString: String
    ): TopicName = TopicName.of(
        projectIdProvider.projectId,
        PUBSUB_TOPIC_REGEX.toRegex().matchEntire(connectionString)!!.groups["topicName"]!!.value
    )
}
