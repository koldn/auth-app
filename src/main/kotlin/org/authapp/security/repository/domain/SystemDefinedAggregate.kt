package org.authapp.security.repository.domain

import org.authapp.security.user.role.SystemDefinedRoles

object SystemDefinedAggregate {
    val ADMIN_ROLE_AGGREGATE = RoleAggregate("admin", setOf(SystemDefinedRoles.ADMIN, SystemDefinedRoles.REVIEWER, SystemDefinedRoles.USER))
    val REVIEWER_ROLE_AGGREGATE = RoleAggregate("reviewer", setOf(SystemDefinedRoles.REVIEWER, SystemDefinedRoles.USER))
    val USER_ROLE_AGGREGATE = RoleAggregate("user", setOf(SystemDefinedRoles.REVIEWER, SystemDefinedRoles.USER))
}