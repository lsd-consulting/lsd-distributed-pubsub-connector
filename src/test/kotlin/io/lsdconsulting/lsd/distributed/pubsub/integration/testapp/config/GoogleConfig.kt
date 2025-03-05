package io.lsdconsulting.lsd.distributed.pubsub.integration.testapp.config

import com.google.api.gax.core.CredentialsProvider
import com.google.api.gax.core.NoCredentialsProvider
import com.google.api.gax.grpc.GrpcTransportChannel
import com.google.api.gax.rpc.FixedTransportChannelProvider
import com.google.api.gax.rpc.TransportChannelProvider
import com.google.cloud.spring.core.GcpProjectIdProvider
import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean

@TestConfiguration
class GoogleConfig {
    @Bean
    fun projectIdProvider(
        @Value("\${spring.cloud.gcp.project-id}") projectId: String
    ) = GcpProjectIdProvider { projectId }

    @Bean
    fun credentialsProvider(): CredentialsProvider = NoCredentialsProvider.create()

    @Bean(destroyMethod = "shutdown")
    fun channel(
        @Value("\${spring.cloud.gcp.pubsub.emulator-host}") emulatorHost: String
    ): ManagedChannel = ManagedChannelBuilder.forTarget(emulatorHost).usePlaintext().build()

    @Bean
    fun channelProvider(channel: ManagedChannel): TransportChannelProvider =
        FixedTransportChannelProvider.create(GrpcTransportChannel.create(channel))
}
