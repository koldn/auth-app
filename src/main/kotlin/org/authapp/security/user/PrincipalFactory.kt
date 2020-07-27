package org.authapp.security.user

import org.authapp.database.domain.DomainUser
import org.authapp.security.feature.spi.Principal

interface PrincipalFactory {
    fun createPrincipal(user: DomainUser): Principal
}