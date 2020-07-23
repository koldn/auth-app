package org.authapp.security.feature.spi

interface Authenticator {
    suspend fun authenticate(credentials: UserCredentials): AuthenticationResult
}