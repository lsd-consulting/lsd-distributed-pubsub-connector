package io.lsdconsulting.lsd.distributed.pubsub.integration

import com.google.api.gax.core.CredentialsProvider
import com.google.api.gax.rpc.TransportChannelProvider
import com.google.cloud.pubsub.v1.SubscriptionAdminClient
import com.google.cloud.pubsub.v1.SubscriptionAdminSettings
import com.google.cloud.pubsub.v1.TopicAdminClient
import com.google.cloud.pubsub.v1.TopicAdminSettings
import com.google.cloud.pubsub.v1.stub.GrpcSubscriberStub
import com.google.cloud.pubsub.v1.stub.SubscriberStubSettings
import com.google.pubsub.v1.*
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.lsdconsulting.lsd.distributed.connector.model.InteractionType.REQUEST
import io.lsdconsulting.lsd.distributed.connector.model.InterceptedInteraction
import io.lsdconsulting.lsd.distributed.pubsub.integration.testapp.TestApplication
import io.lsdconsulting.lsd.distributed.pubsub.repository.InterceptedDocumentPubsubRepository
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.protobuf.ProtoBuf
import org.apache.commons.lang3.RandomStringUtils.secure
import org.awaitility.kotlin.await
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.PubSubEmulatorContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit.MILLIS

@OptIn(ExperimentalSerializationApi::class)
@SpringBootTest(webEnvironment = RANDOM_PORT, classes = [TestApplication::class])
@Testcontainers
@ActiveProfiles("test")
internal class RepositoryIT {
    @Autowired
    private lateinit var underTest: InterceptedDocumentPubsubRepository

    @Autowired
    private lateinit var topicName: TopicName

    @Autowired
    private lateinit var channelProvider: TransportChannelProvider

    @Autowired
    private lateinit var credentialsProvider: CredentialsProvider

    @BeforeEach
    fun createTopicAndSubscription() {
        val topicAdminSettings = TopicAdminSettings
            .newBuilder()
            .setTransportChannelProvider(channelProvider)
            .setCredentialsProvider(credentialsProvider)
            .build()
        TopicAdminClient.create(topicAdminSettings).use { topicAdminClient ->
            topicAdminClient.createTopic(topicName)
        }

        val subscriptionAdminSettings = SubscriptionAdminSettings
            .newBuilder()
            .setTransportChannelProvider(channelProvider)
            .setCredentialsProvider(credentialsProvider)
            .build()
        val subscriptionAdminClient = SubscriptionAdminClient.create(subscriptionAdminSettings)
        val subscriptionName = SubscriptionName.of(topicName.project, "subscriptionId")
        subscriptionAdminClient.createSubscription(
            subscriptionName,
            topicName,
            PushConfig.getDefaultInstance(),
            10
        )
    }

    @Test
    fun `should save and send to pubsub`() {
        val interceptedInteraction = InterceptedInteraction(
            elapsedTime = 20L,
            httpStatus = "OK",
            serviceName = "service",
            target = "target",
            path = "/path",
            httpMethod = "GET",
            body = "body",
            interactionType = REQUEST,
            traceId = secure().nextAlphanumeric(6),
            createdAt = ZonedDateTime.now(ZoneId.of("UTC")).truncatedTo(MILLIS)
        )

        underTest.save(interceptedInteraction)

        val subscriberStubSettings: SubscriberStubSettings = SubscriberStubSettings
            .newBuilder()
            .setTransportChannelProvider(channelProvider)
            .setCredentialsProvider(credentialsProvider)
            .build()
        GrpcSubscriberStub.create(subscriberStubSettings).use { subscriber ->
            val pullRequest: PullRequest = PullRequest
                .newBuilder()
                .setMaxMessages(1)
                .setSubscription(ProjectSubscriptionName.format(topicName.project, "subscriptionId"))
                .build()
            await.untilAsserted {
                val pullResponse: PullResponse = subscriber.pullCallable().call(pullRequest)
                pullResponse.receivedMessagesList shouldHaveSize 1
                val bytes = pullResponse.receivedMessagesList[0].message.data.toByteArray()
                ProtoBuf.decodeFromByteArray<InterceptedInteraction>(bytes) shouldBe interceptedInteraction
            }
        }
    }

    companion object {
        private const val PUBSUB_EMULATOR_IMAGE = "gcr.io/google.com/cloudsdktool/google-cloud-cli:emulators"

        @Container
        private val pubsubEmulatorContainer: PubSubEmulatorContainer = PubSubEmulatorContainer(
            DockerImageName.parse(PUBSUB_EMULATOR_IMAGE)
        )

        @Suppress("unused")
        @DynamicPropertySource
        @JvmStatic
        fun emulatorProperties(registry: DynamicPropertyRegistry) {
            registry.add("spring.cloud.gcp.pubsub.emulator-host", pubsubEmulatorContainer::getEmulatorEndpoint)
        }
    }
}
