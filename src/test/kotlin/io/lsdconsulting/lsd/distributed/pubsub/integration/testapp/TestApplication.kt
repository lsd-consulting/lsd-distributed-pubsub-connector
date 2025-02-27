package io.lsdconsulting.lsd.distributed.pubsub.integration.testapp

import io.lsdconsulting.lsd.distributed.pubsub.config.PubsubLibraryConfig
import io.lsdconsulting.lsd.distributed.pubsub.integration.testapp.config.GoogleConfig
import io.lsdconsulting.lsd.distributed.pubsub.integration.testapp.config.TestRestTemplateConfig
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Import

@SpringBootApplication
@Import(GoogleConfig::class, PubsubLibraryConfig::class, TestRestTemplateConfig::class)
class TestApplication

fun main(args: Array<String>) {
    runApplication<TestApplication>(*args)
}
