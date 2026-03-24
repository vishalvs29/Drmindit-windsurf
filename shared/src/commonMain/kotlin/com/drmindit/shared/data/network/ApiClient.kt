package com.drmindit.shared.data.network

import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

class ApiClient(
    private val baseUrl: String,
    private val apiKey: String,
    private val authToken: String? = null
) {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }

    val httpClient = HttpClient {
        defaultRequest {
            url(baseUrl)
            header("apikey", apiKey)
            header("Content-Type", "application/json")
            authToken?.let { header("Authorization", "Bearer $it") }
        }

        install(ContentNegotiation) {
            json(json)
        }

        install(Auth) {
            bearer {
                loadTokens {
                    authToken?.let {
                        BearerTokens(it, "")
                    }
                }
            }
        }

        install(Logging) {
            logger = Logger.DEFAULT
            level = LogLevel.INFO
        }

        install(DefaultRequest) {
            header("Prefer", "return=representation")
        }

        HttpResponseValidator {
            handleResponseExceptionWithRequest { exception, request ->
                when (exception) {
                    is ClientRequestException -> {
                        when (exception.response.status) {
                            HttpStatusCode.Unauthorized -> throw ApiException.Unauthorized("Authentication failed")
                            HttpStatusCode.Forbidden -> throw ApiException.Forbidden("Access denied")
                            HttpStatusCode.NotFound -> throw ApiException.NotFound("Resource not found")
                            HttpStatusCode.InternalServerError -> throw ApiException.ServerError("Server error")
                            else -> throw ApiException.UnknownError(exception.message ?: "Unknown error")
                        }
                    }
                    is ClientRequestException -> throw ApiException.NetworkError(exception.message ?: "Network error")
                    else -> throw ApiException.UnknownError(exception.message ?: "Unknown error")
                }
            }
        }
    }

    suspend fun get(path: String, parameters: Map<String, String> = emptyMap()) =
        httpClient.get(path) {
            parameters.forEach { (key, value) ->
                parameter(key, value)
            }
        }

    suspend fun post(path: String, body: Any) =
        httpClient.post(path) {
            setBody(body)
        }

    suspend fun put(path: String, body: Any) =
        httpClient.put(path) {
            setBody(body)
        }

    suspend fun delete(path: String) =
        httpClient.delete(path)

    suspend fun patch(path: String, body: Any) =
        httpClient.patch(path) {
            setBody(body)
        }
}

sealed class ApiException(message: String) : Exception(message) {
    class Unauthorized(message: String) : ApiException(message)
    class Forbidden(message: String) : ApiException(message)
    class NotFound(message: String) : ApiException(message)
    class ServerError(message: String) : ApiException(message)
    class NetworkError(message: String) : ApiException(message)
    class UnknownError(message: String) : ApiException(message)
}
