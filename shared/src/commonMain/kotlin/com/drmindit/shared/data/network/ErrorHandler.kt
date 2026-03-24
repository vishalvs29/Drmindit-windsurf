package com.drmindit.shared.data.network

import com.drmindit.shared.data.config.AppConfig
import io.ktor.client.plugins.*
import io.ktor.http.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ErrorHandler {
    
    private val _errorState = MutableStateFlow<ErrorState>(ErrorState.None)
    val errorState: StateFlow<ErrorState> = _errorState.asStateFlow()
    
    private val _networkState = MutableStateFlow(NetworkState.Connected)
    val networkState: StateFlow<NetworkState> = _networkState.asStateFlow()
    
    fun handleError(throwable: Throwable): ErrorState {
        val errorState = when (throwable) {
            is ClientRequestException -> handleClientException(throwable)
            is ServerResponseException -> handleServerException(throwable)
            is HttpRequestTimeoutException -> ErrorState.NetworkError("Request timeout")
            is UnresolvedAddressException -> ErrorState.NetworkError("Network unreachable")
            is NoRouteToHostException -> ErrorState.NetworkError("No route to host")
            is ConnectTimeoutException -> ErrorState.NetworkError("Connection timeout")
            is SocketTimeoutException -> ErrorState.NetworkError("Socket timeout")
            is UnknownHostException -> ErrorState.NetworkError("Unknown host")
            is ApiException -> handleApiException(throwable)
            is SerializationException -> ErrorState.ParseError("Data parsing failed")
            is IllegalArgumentException -> ErrorState.ValidationError(throwable.message ?: "Invalid input")
            is SecurityException -> ErrorState.SecurityError(throwable.message ?: "Security violation")
            else -> ErrorState.UnknownError(throwable.message ?: "Unknown error occurred")
        }
        
        _errorState.value = errorState
        
        // Log error if in debug mode
        if (AppConfig.isDebugMode) {
            logError(errorState, throwable)
        }
        
        return errorState
    }
    
    private fun handleClientException(exception: ClientRequestException): ErrorState {
        return when (exception.response.status) {
            HttpStatusCode.BadRequest -> ErrorState.ValidationError("Invalid request data")
            HttpStatusCode.Unauthorized -> ErrorState.AuthenticationError("Authentication required")
            HttpStatusCode.Forbidden -> ErrorState.AuthorizationError("Access denied")
            HttpStatusCode.NotFound -> ErrorState.NotFoundError("Resource not found")
            HttpStatusCode.Conflict -> ErrorState.ConflictError("Resource conflict")
            HttpStatusCode.TooManyRequests -> ErrorState.RateLimitError("Too many requests")
            HttpStatusCode.UnprocessableEntity -> ErrorState.ValidationError("Invalid data format")
            else -> ErrorState.ClientError("Client error: ${exception.response.status}")
        }
    }
    
    private fun handleServerException(exception: ServerResponseException): ErrorState {
        return when (exception.response.status) {
            HttpStatusCode.InternalServerError -> ErrorState.ServerError("Internal server error")
            HttpStatusCode.BadGateway -> ErrorState.ServerError("Bad gateway")
            HttpStatusCode.ServiceUnavailable -> ErrorState.ServerError("Service unavailable")
            HttpStatusCode.GatewayTimeout -> ErrorState.ServerError("Gateway timeout")
            HttpStatusCode.NotImplemented -> ErrorState.ServerError("Feature not implemented")
            else -> ErrorState.ServerError("Server error: ${exception.response.status}")
        }
    }
    
    private fun handleApiException(exception: ApiException): ErrorState {
        return when (exception) {
            is ApiException.Unauthorized -> ErrorState.AuthenticationError(exception.message)
            is ApiException.Forbidden -> ErrorState.AuthorizationError(exception.message)
            is ApiException.NotFound -> ErrorState.NotFoundError(exception.message)
            is ApiException.ServerError -> ErrorState.ServerError(exception.message)
            is ApiException.NetworkError -> ErrorState.NetworkError(exception.message)
            is ApiException.UnknownError -> ErrorState.UnknownError(exception.message)
        }
    }
    
    fun clearError() {
        _errorState.value = ErrorState.None
    }
    
    fun setNetworkState(state: NetworkState) {
        _networkState.value = state
    }
    
    private fun logError(errorState: ErrorState, throwable: Throwable) {
        when (errorState) {
            is ErrorState.NetworkError -> {
                println("🔴 Network Error: ${errorState.message}")
                println("   Exception: ${throwable::class.simpleName}")
            }
            is ErrorState.ServerError -> {
                println("🔴 Server Error: ${errorState.message}")
                println("   Exception: ${throwable::class.simpleName}")
            }
            is ErrorState.AuthenticationError -> {
                println("🔴 Authentication Error: ${errorState.message}")
            }
            is ErrorState.AuthorizationError -> {
                println("🔴 Authorization Error: ${errorState.message}")
            }
            is ErrorState.ValidationError -> {
                println("🔴 Validation Error: ${errorState.message}")
            }
            is ErrorState.NotFoundError -> {
                println("🔴 Not Found Error: ${errorState.message}")
            }
            is ErrorState.ParseError -> {
                println("🔴 Parse Error: ${errorState.message}")
            }
            is ErrorState.SecurityError -> {
                println("🔴 Security Error: ${errorState.message}")
            }
            is ErrorState.RateLimitError -> {
                println("🔴 Rate Limit Error: ${errorState.message}")
            }
            is ErrorState.ConflictError -> {
                println("🔴 Conflict Error: ${errorState.message}")
            }
            is ErrorState.ClientError -> {
                println("🔴 Client Error: ${errorState.message}")
            }
            is ErrorState.UnknownError -> {
                println("🔴 Unknown Error: ${errorState.message}")
                println("   Exception: ${throwable::class.simpleName}")
                throwable.printStackTrace()
            }
            ErrorState.None -> {
                // No error to log
            }
        }
    }
    
    fun getErrorMessage(errorState: ErrorState): String {
        return when (errorState) {
            is ErrorState.NetworkError -> "Network connection issue. Please check your internet connection."
            is ErrorState.ServerError -> "Server is temporarily unavailable. Please try again later."
            is ErrorState.AuthenticationError -> "Please sign in to continue."
            is ErrorState.AuthorizationError -> "You don't have permission to perform this action."
            is ErrorState.ValidationError -> "Please check your input and try again."
            is ErrorState.NotFoundError -> "The requested resource was not found."
            is ErrorState.ParseError -> "There was an error processing the data."
            is ErrorState.SecurityError -> "Security validation failed."
            is ErrorState.RateLimitError -> "Too many requests. Please wait a moment and try again."
            is ErrorState.ConflictError -> "There was a conflict with your request."
            is ErrorState.ClientError -> "There was an error with your request."
            is ErrorState.UnknownError -> "An unexpected error occurred. Please try again."
            ErrorState.None -> ""
        }
    }
    
    fun shouldRetry(errorState: ErrorState): Boolean {
        return when (errorState) {
            is ErrorState.NetworkError,
            is ErrorState.ServerError,
            is ErrorState.RateLimitError -> true
            else -> false
        }
    }
    
    fun getRetryDelay(attempt: Int, errorState: ErrorState): Long {
        val baseDelay = when (errorState) {
            is ErrorState.RateLimitError -> 5000L // 5 seconds
            is ErrorState.ServerError -> 2000L // 2 seconds
            is ErrorState.NetworkError -> 1000L // 1 second
            else -> 1000L
        }
        
        // Exponential backoff with jitter
        return (baseDelay * (1 shl attempt) + (0..1000).random()).coerceAtMost(30000L)
    }
}

sealed class ErrorState {
    object None : ErrorState()
    
    data class NetworkError(val message: String) : ErrorState()
    data class ServerError(val message: String) : ErrorState()
    data class AuthenticationError(val message: String) : ErrorState()
    data class AuthorizationError(val message: String) : ErrorState()
    data class ValidationError(val message: String) : ErrorState()
    data class NotFoundError(val message: String) : ErrorState()
    data class ParseError(val message: String) : ErrorState()
    data class SecurityError(val message: String) : ErrorState()
    data class RateLimitError(val message: String) : ErrorState()
    data class ConflictError(val message: String) : ErrorState()
    data class ClientError(val message: String) : ErrorState()
    data class UnknownError(val message: String) : ErrorState()
}

sealed class NetworkState {
    object Connected : NetworkState()
    object Disconnected : NetworkState()
    object Connecting : NetworkState()
    object Slow : NetworkState()
}

// Extension functions for easier error handling
fun Throwable.toErrorState(): ErrorState {
    return when (this) {
        is ClientRequestException -> when (this.response.status) {
            HttpStatusCode.BadRequest -> ErrorState.ValidationError("Invalid request data")
            HttpStatusCode.Unauthorized -> ErrorState.AuthenticationError("Authentication required")
            HttpStatusCode.Forbidden -> ErrorState.AuthorizationError("Access denied")
            HttpStatusCode.NotFound -> ErrorState.NotFoundError("Resource not found")
            else -> ErrorState.ClientError("Client error: ${this.response.status}")
        }
        is ServerResponseException -> ErrorState.ServerError("Server error: ${this.response.status}")
        is HttpRequestTimeoutException -> ErrorState.NetworkError("Request timeout")
        is UnresolvedAddressException -> ErrorState.NetworkError("Network unreachable")
        is ConnectTimeoutException -> ErrorState.NetworkError("Connection timeout")
        is SocketTimeoutException -> ErrorState.NetworkError("Socket timeout")
        is UnknownHostException -> ErrorState.NetworkError("Unknown host")
        is ApiException -> when (this) {
            is ApiException.Unauthorized -> ErrorState.AuthenticationError(this.message)
            is ApiException.Forbidden -> ErrorState.AuthorizationError(this.message)
            is ApiException.NotFound -> ErrorState.NotFoundError(this.message)
            is ApiException.ServerError -> ErrorState.ServerError(this.message)
            is ApiException.NetworkError -> ErrorState.NetworkError(this.message)
            is ApiException.UnknownError -> ErrorState.UnknownError(this.message)
        }
        is SerializationException -> ErrorState.ParseError("Data parsing failed")
        is IllegalArgumentException -> ErrorState.ValidationError(this.message ?: "Invalid input")
        is SecurityException -> ErrorState.SecurityError(this.message ?: "Security violation")
        else -> ErrorState.UnknownError(this.message ?: "Unknown error occurred")
    }
}
