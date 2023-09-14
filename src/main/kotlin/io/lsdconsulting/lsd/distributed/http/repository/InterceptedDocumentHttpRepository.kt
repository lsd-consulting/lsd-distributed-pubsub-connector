package io.lsdconsulting.lsd.distributed.http.repository

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import io.lsdconsulting.lsd.distributed.connector.model.InterceptedInteraction
import io.lsdconsulting.lsd.distributed.connector.repository.InterceptedDocumentRepository
import lsd.logging.log
import org.apache.http.HttpResponse
import org.apache.http.HttpStatus.SC_OK
import org.apache.http.client.fluent.Request
import org.apache.http.entity.ContentType.APPLICATION_JSON
import java.io.BufferedReader
import java.net.SocketTimeoutException


class InterceptedDocumentHttpRepository(
    connectionString: String,
    private val connectionTimeout: Int,
    private val objectMapper: ObjectMapper
) : InterceptedDocumentRepository {

    private val uri = getUri(connectionString)

    private fun getUri(connectionString: String) =
        if (connectionString.endsWith("/")) "${connectionString}lsds" else "$connectionString/lsds"

    override fun save(interceptedInteraction: InterceptedInteraction) {
        val response: HttpResponse

        try {
            response = Request.Post(uri)
                .connectTimeout(connectionTimeout)
                .socketTimeout(connectionTimeout)
                .bodyString(objectMapper.writeValueAsString(interceptedInteraction), APPLICATION_JSON)
                .execute()
                .returnResponse()
        } catch (e: SocketTimeoutException) {
            log().warn("Connection to $uri timed out. Dropping interceptedInteraction: $interceptedInteraction")
            return
        }

        val statusCode = response.statusLine.statusCode
        if (statusCode != SC_OK) {
            log().warn("Unable to call $uri - received: $statusCode. Lost interceptedInteraction: $interceptedInteraction")
        }
    }

    override fun findByTraceIds(vararg traceId: String): List<InterceptedInteraction> {
        val response: HttpResponse

        try {
            response = Request.Get(uri + "?traceIds=" + traceId.joinToString(separator = "&traceIds="))
                .connectTimeout(connectionTimeout)
                .socketTimeout(connectionTimeout)
                .execute()
                .returnResponse()
        } catch (e: SocketTimeoutException) {
            log().warn("Connection to $uri timed out.")
            throw e
        }

        val statusCode = response.statusLine.statusCode
        if (statusCode != SC_OK) {
            log().warn("Unable to call $uri - received: $statusCode.")
        }

        val use = response.entity.content.bufferedReader().use(BufferedReader::readText)
        return objectMapper.readValue(use, object: TypeReference<List<InterceptedInteraction>>(){})
    }

    override fun isActive() = true
}
