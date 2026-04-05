package com.drmindit.shared.data.network

actual class UnresolvedAddressException : Exception()
actual class ConnectTimeoutException actual constructor(message: String, cause: Throwable?) : Exception(message, cause)
actual class SocketTimeoutException : Exception()
actual class UnknownHostException : Exception()
actual class NoRouteToHostException : Exception()
actual class SecurityException : Exception()
