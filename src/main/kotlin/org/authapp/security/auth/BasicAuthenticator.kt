package org.authapp.security.auth

import org.authapp.security.encrypt.PasswordCoder
import org.authapp.security.feature.*
import org.authapp.security.user.PrincipalLoader

class BasicAuthenticator(private val principalLoader: PrincipalLoader, private val passwordCoder: PasswordCoder) : Authenticator {

    data class BasicCredentials(val userName: String, val password: String) : UserCredentials

    override suspend fun authenticate(credentials: UserCredentials): AuthenticationResult {
        if (credentials !is BasicCredentials) {
            return UnsupportedAuthenticationCredentials
        }
        val userName = credentials.userName
        val rawPassword = credentials.password
        val principal: Principal? = principalLoader.loadPrincipal(userName)
        if (principal == null) {
            return FailedAuthentication("User $userName not found")
        }
        if (!passwordCoder.matches(rawPassword, principal.password())) {
            return FailedAuthentication("Invalid password")
        }
        return SuccessFullAuthentication(principal)
    }
}