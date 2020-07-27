package org.authapp.security.user

import org.authapp.database.domain.RoleAggregate
import org.authapp.security.feature.spi.Principal

class UserPrincipal(private val userName: String, private val roleAggregate: RoleAggregate) : Principal {
    override fun userName() = userName
    override fun hasRole(role: String): Boolean = roleAggregate.roles.contains(role)
}

