package org.authapp.security.user

import org.authapp.authfeature.spi.Principal
import org.authapp.database.domain.DomainUser

interface PrincipalFactory {
    fun createPrincipal(user: DomainUser): Principal
}