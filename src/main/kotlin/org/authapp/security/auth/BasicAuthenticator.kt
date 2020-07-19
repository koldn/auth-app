package org.authapp.security.auth

import io.ktor.application.ApplicationCall
import io.ktor.http.HttpHeaders
import org.authapp.security.encrypt.PasswordCoder
import org.authapp.security.user.Principal
import org.authapp.security.user.PrincipalLoader
import java.nio.charset.StandardCharsets
import java.util.*

class BasicAuthenticator(private val principalLoader: PrincipalLoader, private val passwordCoder: PasswordCoder) : Authenticator {
    override suspend fun authenticate(call: ApplicationCall): AuthenticationResult {
        val authHeader = call.request.headers[HttpHeaders.Authorization]
        if (authHeader == null) return FailedAuthentication("${HttpHeaders.Authorization} is missing")
        //skip "Basic " and decode
        val decodedAuthData = Base64.getDecoder().decode(authHeader.substring(6)).toString(StandardCharsets.UTF_8)
        val split = decodedAuthData.split(":")
        val userName = split[0]
        val rawPassword = split[1]
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