package org.authapp.security.auth

import io.ktor.application.ApplicationCall
import org.authapp.security.user.Principal

sealed class AuthenticationResult
class SuccessFullAuthentication(val principal: Principal) : AuthenticationResult()
class FailedAuthentication(val errorMessage: String) : AuthenticationResult()

interface Authenticator {
    suspend fun authenticate(call: ApplicationCall): AuthenticationResult
}