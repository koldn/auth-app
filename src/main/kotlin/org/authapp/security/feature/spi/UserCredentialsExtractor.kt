package org.authapp.security.feature.spi

import io.ktor.application.ApplicationCall

interface UserCredentialsExtractor {
    fun extract(applicationCall: ApplicationCall): UserCredentials?
}