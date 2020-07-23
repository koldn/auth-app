package org.authapp.security.feature.ext

import io.ktor.application.ApplicationCall
import org.authapp.security.feature.AuthConstants
import org.authapp.security.feature.spi.Principal

fun ApplicationCall.getPrincipal(): Principal {
    return this.attributes[AuthConstants.principalKey]
}