package io.lsdconsulting.lsd.distributed.http.config

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import io.lsdconsulting.lsd.distributed.access.repository.InterceptedDocumentRepository
import io.lsdconsulting.lsd.distributed.http.repository.InterceptedDocumentHttpRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@ConditionalOnProperty(name = ["lsd.dist.db.connectionString"])
open class HttpLibraryConfig {
    @Bean
    open fun interceptedDocumentRepository(
        @Value("\${lsd.dist.db.connectionString}") dbConnectionString: String,
        @Value("\${lsd.dist.http.connectionTimeout.millis:#{2000}}") connectionTimeout: Int
    ): InterceptedDocumentRepository {
        return InterceptedDocumentHttpRepository(dbConnectionString, connectionTimeout, objectMapper())
    }

    @Bean
    @ConditionalOnMissingBean(ObjectMapper::class)
    open fun objectMapper(): ObjectMapper {
        val mapper = ObjectMapper()
        mapper.registerModule(JavaTimeModule())
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        return mapper
    }
}
