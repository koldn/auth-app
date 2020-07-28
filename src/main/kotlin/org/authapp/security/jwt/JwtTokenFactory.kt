package org.authapp.security.jwt

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*

/**
 * Token factory implementation based on JWT
 * @property secretToken token from configuration properties
 */
class JwtTokenFactory(
        private val tokenLifetime: Long,
        private val issuer: String,
        private val secretToken: String
) : TokenFactory {

    override fun createToken(userName: String): String {
        return Jwts.builder()
                .addClaims(mapOf(Claims.AUDIENCE to userName))
                .setIssuedAt(Date())
                .setExpiration(Date.from(Instant.now().plus(tokenLifetime, ChronoUnit.SECONDS)))
                .setIssuer(issuer)
                .signWith(SignatureAlgorithm.HS512, secretToken)
                .compact()
    }
}