package org.authapp.security.jwt

interface TokenFactory {
    fun createToken(userName: String): String
}