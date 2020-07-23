package org.authapp.security.auth

import io.ktor.application.ApplicationCall
import io.ktor.http.HttpHeaders
import org.authapp.security.feature.spi.UserCredentials
import org.authapp.security.feature.spi.UserCredentialsExtractor
import java.nio.charset.StandardCharsets
import java.util.*

object DefaultUserCredentialsExtractor : UserCredentialsExtractor {
    override fun extract(applicationCall: ApplicationCall): UserCredentials? {
        val authHeader = applicationCall.request.headers[HttpHeaders.Authorization] ?: return null
        if (authHeader.startsWith("Basic ")) {
            //skip "Basic " and decode
            val decodedAuthData = Base64.getDecoder().decode(authHeader.substring(6)).toString(StandardCharsets.UTF_8)
            val split = decodedAuthData.split(":")
            return BasicAuthenticator.BasicCredentials(split[0], split[1])
        }
        return null
    }
}