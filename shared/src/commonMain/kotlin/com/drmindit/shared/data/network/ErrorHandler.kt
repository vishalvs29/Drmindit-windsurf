package com.drmindit.shared.data.network

import io.ktor.client.plugins.*
import io.ktor.http.*
import io.ktor.client.statement.*
import kotlinx.coroutines.CancellationException

/**
 * Network Error Handler
 * Handles various network exceptions and converts them to ErrorState
 */
class ErrorHandler {
    
    fun handleError(exception: Throwable): ErrorState {
        return when (exception) {
            is CancellationException -> throw exception // Re-throw cancellation
            is ResponseException -> when (exception.response.status) {
                HttpStatusCode.Unauthorized -> ErrorState.AuthenticationError("Unauthorized access")
                HttpStatusCode.Forbidden -> ErrorState.AuthorizationError("Access forbidden")
                HttpStatusCode.NotFound -> ErrorState.NotFoundError("Resource not found")
                HttpStatusCode.BadRequest -> ErrorState.ValidationError("Invalid request")
                HttpStatusCode.InternalServerError -> ErrorState.ServerError("Server error")
                HttpStatusCode.BadGateway -> ErrorState.ServerError("Server unavailable")
                HttpStatusCode.ServiceUnavailable -> ErrorState.ServerError("Service unavailable")
                else -> ErrorState.UnknownError("HTTP ${exception.response.status.value}")
            }
            else -> {
                when {
                    exception.message?.contains("UnresolvedAddress") == true -> 
                        ErrorState.NetworkError("Unable to resolve server address")
                    exception.message?.contains("UnknownHost") == true -> 
                        ErrorState.NetworkError("Unknown host")
                    exception.message?.contains("timeout") == true -> 
                        ErrorState.NetworkError("Connection timeout")
                    exception.message?.contains("Connection") == true -> 
                        ErrorState.NetworkError("Connection failed")
                    else -> ErrorState.UnknownError(exception.message ?: "Unknown error occurred")
                }
            }
        }
    }
}

/**
 * Sealed class representing different error states
 */
sealed class ErrorState {
    data class NetworkError(val message: String) : ErrorState()
    data class AuthenticationError(val message: String) : ErrorState()
    data class AuthorizationError(val message: String) : ErrorState()
    data class NotFoundError(val message: String) : ErrorState()
    data class ValidationError(val message: String) : ErrorState()
    data class ServerError(val message: String) : ErrorState()
    data class UnknownError(val message: String) : ErrorState()
}
