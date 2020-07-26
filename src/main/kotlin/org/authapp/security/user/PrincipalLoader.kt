package org.authapp.security.user

import org.authapp.security.feature.spi.Principal

interface PrincipalLoader {
    fun loadPrincipal(userName: String): Principal?
}