package io.lsdconsulting.lsd.distributed.pubsub.integration.testapp.config

import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.context.annotation.Bean

@TestConfiguration
class TestRestTemplateConfig {
    @Bean
    fun testRestTemplate() = TestRestTemplate()
}
