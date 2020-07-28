package org.authapp.security.jwt

import io.jsonwebtoken.JwtException
import io.jsonwebtoken.Jwts
import org.authapp.authfeature.spi.*
import org.authapp.database.domain.DomainUser
import org.authapp.database.repository.DataRepository
import org.authapp.security.auth.AuthenticatorCodes
import org.authapp.security.user.PrincipalFactory
import org.slf4j.LoggerFactory

class JwtAuthenticator(
        private val principalFactory: PrincipalFactory,
        private val userRepository: DataRepository<DomainUser>,
        private val issuer: String,
        private val secretToken: String
) : Authenticator {
    data class TokenCredentials(val token: String) : UserCredentials

    companion object {
        private val LOGGER = LoggerFactory.getLogger(JwtAuthenticator::class.java)
    }

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
            val domainUser = userRepository.findById(userName)
                    ?: return FailedAuthentication("User $userName not found")
            return SuccessFullAuthentication(principalFactory.createPrincipal(domainUser))


        } catch (e: JwtException) {
            LOGGER.error("Invalid token {}", e.toString(), e)
        }
        return FailedAuthentication("Invalid token")
    }

    override fun code(): String = AuthenticatorCodes.TOKEN
}