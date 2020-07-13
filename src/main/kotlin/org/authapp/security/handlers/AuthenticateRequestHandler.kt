package org.authapp.security.handlers

import io.ktor.application.ApplicationCall
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.response.respondText
import org.authapp.ApplicationCallHandler
import org.authapp.security.TokenFactory
import java.nio.charset.StandardCharsets
import java.util.*

/**
 * An application call handler which is mapped to <code>/authenticate</code> path
 * @property tokenFactory - an instance of token factory used for token creation on successful authentication
 */
class AuthenticateRequestHandler(private val tokenFactory: TokenFactory) : ApplicationCallHandler {
    override suspend fun handleCall(applicationCall: ApplicationCall) {
        val authHeader = applicationCall.request.headers[HttpHeaders.Authorization]
        if (authHeader == null) {
            applicationCall.respond(HttpStatusCode.BadRequest, "No ${HttpHeaders.Authorization} header")
            return
        }
        //skip "Basic " and decode
        val decodedAuthData = Base64.getDecoder().decode(authHeader.substring(6)).toString(StandardCharsets.UTF_8)
        val split = decodedAuthData.split(":")
        val userName = split[0]
        val rawPassword = split[1]
        //TODO check username and password in some kind of repository
        applicationCall.respondText(tokenFactory.createToken(userName))
    }
}