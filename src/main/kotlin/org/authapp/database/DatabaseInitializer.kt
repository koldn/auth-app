package org.authapp.database

import org.authapp.database.domain.Role
import org.authapp.database.domain.RolesTable
import org.authapp.database.domain.UserTable
import org.authapp.security.user.role.SystemRoles
import org.jetbrains.exposed.sql.SchemaUtils

object DatabaseInitializer {
    fun initTables() {
        SchemaUtils.create(UserTable, RolesTable)
    }

    fun initializeSystemRoles() {
        if (null == Role.findById(SystemRoles.USER)) {
            Role.new(SystemRoles.USER) {}
        }
        if (null == Role.findById(SystemRoles.REVIEWER)) {
            Role.new(SystemRoles.REVIEWER) {
                includes = SystemRoles.USER
            }
        }
        if (null == Role.findById(SystemRoles.ADMIN)) {
            Role.new(SystemRoles.ADMIN) {
                includes = SystemRoles.REVIEWER
            }
        }
    }
}