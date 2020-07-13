package org.authapp.security.jwt

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import org.authapp.security.TokenFactory
import java.util.*

/**
 * Token factory implementation based on JWT
 * @property secretToken token from configuration properties
 */
class JwtTokenFactory(private val secretToken: String) : TokenFactory {

    override fun createToken(userName: String): String {
        return Jwts.builder()
                .addClaims(mapOf("aud" to userName))
                .setIssuedAt(Date())
                .signWith(SignatureAlgorithm.HS512, secretToken)
                .compact()
    }
}