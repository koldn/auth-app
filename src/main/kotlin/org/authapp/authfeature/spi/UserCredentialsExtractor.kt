package org.authapp.authfeature.spi

import io.ktor.application.ApplicationCall

interface UserCredentialsExtractor {
    fun extract(applicationCall: ApplicationCall): UserCredentials?
}