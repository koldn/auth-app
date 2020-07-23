package org.authapp.security.jwt

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import java.util.*

/**
 * Token factory implementation based on JWT
 * @property secretToken token from configuration properties
 */
class JwtTokenFactory(private val issuer: String, private val secretToken: String) : TokenFactory {

    override fun createToken(userName: String): String {
        return Jwts.builder()
                .addClaims(mapOf(Claims.AUDIENCE to userName))
                .setIssuedAt(Date())
                .setIssuer(issuer)
                .signWith(SignatureAlgorithm.HS512, secretToken)
                .compact()
    }
}