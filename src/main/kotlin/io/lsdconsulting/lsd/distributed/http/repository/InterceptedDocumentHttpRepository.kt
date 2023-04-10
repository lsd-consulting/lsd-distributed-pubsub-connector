package io.lsdconsulting.lsd.distributed.http.repository

import com.fasterxml.jackson.databind.ObjectMapper
import io.lsdconsulting.lsd.distributed.access.model.InterceptedInteraction
import io.lsdconsulting.lsd.distributed.access.repository.InterceptedDocumentRepository
import io.lsdconsulting.lsd.distributed.http.config.log
import org.apache.http.HttpResponse
import org.apache.http.client.fluent.Request
import org.apache.http.entity.ContentType.APPLICATION_JSON
import java.net.SocketTimeoutException


class InterceptedDocumentHttpRepository(
    connectionString: String,
    private val connectionTimeout: Int,
    private val objectMapper: ObjectMapper
) : InterceptedDocumentRepository {

    private val postUri = "$connectionString/lsd"

    override fun save(interceptedInteraction: InterceptedInteraction) {
        val response: HttpResponse

        try {
            response = Request.Post(postUri)
                .connectTimeout(connectionTimeout)
                .socketTimeout(connectionTimeout)
                .bodyString(objectMapper.writeValueAsString(interceptedInteraction), APPLICATION_JSON)
                .execute()
                .returnResponse()
        } catch (e: SocketTimeoutException) {
            log().warn("Connection to $postUri timed out. Dropping interceptedInteraction: $interceptedInteraction")
            return
        }

        val statusCode = response.statusLine.statusCode
        if (statusCode != 200) {
            log().warn("Unable to call $postUri - received: $statusCode. Lost interceptedInteraction: $interceptedInteraction")
        }
    }

    override fun findByTraceIds(vararg traceId: String): List<InterceptedInteraction> {
        return ArrayList()
    }
}
