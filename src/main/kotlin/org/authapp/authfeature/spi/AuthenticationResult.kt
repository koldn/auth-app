package org.authapp.authfeature.spi

sealed class AuthenticationResult
class SuccessFullAuthentication(val principal: Principal) : AuthenticationResult()
class FailedAuthentication(val errorMessage: String) : AuthenticationResult()
