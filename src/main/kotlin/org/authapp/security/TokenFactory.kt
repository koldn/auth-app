package org.authapp.security

interface TokenFactory {
    fun createToken(userName: String): String
}