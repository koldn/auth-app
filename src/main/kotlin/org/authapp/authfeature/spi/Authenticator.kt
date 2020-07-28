package org.authapp.authfeature.spi

interface Authenticator {
    suspend fun authenticate(credentials: UserCredentials): AuthenticationResult
    fun code(): String
}