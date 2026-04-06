package com.drmindit.shared.data.network

import io.ktor.client.plugins.*
import io.ktor.http.*
import io.ktor.client.statement.*
import io.ktor.client.call.*
import io.ktor.util.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ErrorHandlerTest {

    private val errorHandler = ErrorHandler()

    @Test
    fun testHandleUnresolvedAddressException() {
        val exception = RuntimeException("UnresolvedAddressException: Unable to resolve host")
        val errorState = errorHandler.handleError(exception)
        
        assertTrue(errorState is ErrorState.NetworkError)
        assertEquals("Unable to resolve server address", (errorState as ErrorState.NetworkError).message)
    }

    @Test
    fun testHandleUnknownHostException() {
        val exception = RuntimeException("UnknownHostException: Host not found")
        val errorState = errorHandler.handleError(exception)
        
        assertTrue(errorState is ErrorState.NetworkError)
        assertEquals("Unknown host", (errorState as ErrorState.NetworkError).message)
    }

    @Test
    fun testHandleTimeoutException() {
        val exception = RuntimeException("timeout occurred")
        val errorState = errorHandler.handleError(exception)
        
        assertTrue(errorState is ErrorState.NetworkError)
        assertEquals("Connection timeout", (errorState as ErrorState.NetworkError).message)
    }

    @Test
    fun testHandleConnectionException() {
        val exception = RuntimeException("ConnectException: Connection refused")
        val errorState = errorHandler.handleError(exception)
        
        assertTrue(errorState is ErrorState.NetworkError)
        assertEquals("Connection failed", (errorState as ErrorState.NetworkError).message)
    }

    @Test
    fun testHandleUnauthorizedResponse() {
        val exception = RuntimeException("HTTP 401 Unauthorized")
        val errorState = errorHandler.handleError(exception)
        
        assertTrue(errorState is ErrorState.UnknownError)
        assertEquals("HTTP 401 Unauthorized", (errorState as ErrorState.UnknownError).message)
    }

    @Test
    fun testHandleNotFoundResponse() {
        val exception = RuntimeException("HTTP 404 Not found")
        val errorState = errorHandler.handleError(exception)
        
        assertTrue(errorState is ErrorState.UnknownError)
        assertEquals("HTTP 404 Not found", (errorState as ErrorState.UnknownError).message)
    }

    @Test
    fun testHandleServerError() {
        val exception = RuntimeException("HTTP 500 Internal server error")
        val errorState = errorHandler.handleError(exception)
        
        assertTrue(errorState is ErrorState.UnknownError)
        assertEquals("HTTP 500 Internal server error", (errorState as ErrorState.UnknownError).message)
    }

    @Test
    fun testHandleUnknownError() {
        val exception = RuntimeException("Some unknown error")
        val errorState = errorHandler.handleError(exception)
        
        assertTrue(errorState is ErrorState.UnknownError)
        assertEquals("Some unknown error", (errorState as ErrorState.UnknownError).message)
    }
}
