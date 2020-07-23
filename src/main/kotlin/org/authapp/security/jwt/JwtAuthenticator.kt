package org.authapp.security.jwt

import io.jsonwebtoken.JwtException
import io.jsonwebtoken.Jwts
import org.authapp.security.auth.AuthenticatorCodes
import org.authapp.security.feature.spi.*
import org.authapp.security.user.PrincipalLoader

class JwtAuthenticator(private val principalLoader: PrincipalLoader, private val issuer: String, private val secretToken: String) : Authenticator {
    data class TokenCredentials(val token: String) : UserCredentials

    override suspend fun authenticate(credentials: UserCredentials): AuthenticationResult {
        if (credentials !is TokenCredentials) {
            return FailedAuthentication("Unsupported credentials")
        }
        try {
            val parseClaimsJws = Jwts.parser().setSigningKey(secretToken).parseClaimsJws(credentials.token)
            val body = parseClaimsJws.body
            if (body.issuer != issuer) {
                return FailedAuthentication("Invalid token")
            }
            val userName = body.audience
            val principal = principalLoader.loadPrincipal(userName)
                    ?: return FailedAuthentication("User $userName not found")
            return SuccessFullAuthentication(principal)


        } catch (e: JwtException) {
            //TODO log me!
        }
        return FailedAuthentication("Invalid token")
    }

    override fun code(): String = AuthenticatorCodes.TOKEN
}