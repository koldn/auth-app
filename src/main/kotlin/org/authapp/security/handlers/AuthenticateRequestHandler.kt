package org.authapp.security.handlers

import io.ktor.application.ApplicationCall
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.response.respondText
import org.authapp.ApplicationCallHandler
import org.authapp.security.TokenFactory
import org.authapp.security.auth.Authenticator
import org.authapp.security.auth.FailedAuthentication
import org.authapp.security.auth.SuccessFullAuthentication

/**
 * An application call handler which is mapped to <code>/authenticate</code> path
 * @property tokenFactory - an instance of token factory used for token creation on successful authentication
 * //TODO authentication logic should be moved to some pipeline phase.
 */
class AuthenticateRequestHandler(private val authenticator: Authenticator, private val tokenFactory: TokenFactory) : ApplicationCallHandler {
    override suspend fun handleCall(applicationCall: ApplicationCall) {
        when (val result = authenticator.authenticate(applicationCall)) {
            is SuccessFullAuthentication -> applicationCall.respondText(tokenFactory.createToken(result.principal.userName()))
            is FailedAuthentication -> applicationCall.respond(HttpStatusCode.Unauthorized, result.errorMessage)
        }
    }
}