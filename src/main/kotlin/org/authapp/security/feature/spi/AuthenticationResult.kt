package org.authapp.security.feature.spi

sealed class AuthenticationResult
class SuccessFullAuthentication(val principal: Principal) : AuthenticationResult()
class FailedAuthentication(val errorMessage: String) : AuthenticationResult()
object UnsupportedAuthenticationCredentials : AuthenticationResult()