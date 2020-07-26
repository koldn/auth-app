package org.authapp.security.repository

import org.authapp.security.repository.domain.RoleAggregateTable
import org.authapp.security.repository.domain.RoleTable
import org.authapp.security.repository.domain.UserTable
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

class DatabaseInitializer(private val access: DataBaseAccess) {
    fun initTables() {
        transaction(access.database) {
            SchemaUtils.create(UserTable, RoleAggregateTable, RoleTable)
        }
    }
}