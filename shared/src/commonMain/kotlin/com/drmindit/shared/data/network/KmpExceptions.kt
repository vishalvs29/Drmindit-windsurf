package com.drmindit.shared.data.network

/**
 * Common exceptions for multiplatform networking.
 * These are defined as expect classes to allow catching platform-specific exceptions
 * in common code.
 */
expect class UnresolvedAddressException : Exception
expect class ConnectTimeoutException : Exception
expect class SocketTimeoutException : Exception
expect class UnknownHostException : Exception
expect class NoRouteToHostException : Exception
expect class SecurityException : Exception
