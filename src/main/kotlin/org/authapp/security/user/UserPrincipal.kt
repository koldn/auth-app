package org.authapp.security.user

import org.authapp.authfeature.spi.Principal
import org.authapp.database.domain.RoleAggregate

class UserPrincipal(private val userName: String, private val roleAggregate: RoleAggregate) : Principal {
    override fun userName() = userName
    override fun hasRole(role: String): Boolean = roleAggregate.roles.contains(role)
}

