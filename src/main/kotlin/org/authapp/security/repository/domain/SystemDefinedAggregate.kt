package org.authapp.security.repository.domain

import org.authapp.security.user.role.SystemDefinedRoles.ADMIN
import org.authapp.security.user.role.SystemDefinedRoles.REVIEWER
import org.authapp.security.user.role.SystemDefinedRoles.USER

object SystemDefinedAggregate {
    val ADMIN_ROLE_AGGREGATE = RoleAggregate("admin", setOf(ADMIN, REVIEWER, USER))
    val REVIEWER_ROLE_AGGREGATE = RoleAggregate("reviewer", setOf(REVIEWER, USER))
    val USER_ROLE_AGGREGATE = RoleAggregate("user", setOf(USER))
}