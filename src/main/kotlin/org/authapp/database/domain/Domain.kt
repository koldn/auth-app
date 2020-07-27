package org.authapp.database.domain

import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.Column

class DomainUser(val userName: String, var password: String, var userRole: String)
class RoleAggregate(val roles: Set<String>)

//Exposed
//Tables
object UserTable : IdTable<String>(name = "tbl_users") {
    override val id = varchar("username", length = 255).entityId()
    val password: Column<String> = varchar("password", length = 255)
    val userRole: Column<String> = varchar("user_role", length = 255)
}

object RolesTable : IdTable<String>(name = "tbl_role") {
    override val id: Column<EntityID<String>> = varchar("role_name", length = 255).entityId()
    val includes = varchar("includes", length = 64).index("idx_includes").nullable()
}

//Entities
class UserEntity(id: EntityID<String>) : Entity<String>(id) {
    companion object : EntityClass<String, UserEntity>(UserTable)
    var password by UserTable.password
    var userRole by UserTable.userRole
}

class Role(id: EntityID<String>) : Entity<String>(id) {
    companion object : EntityClass<String, Role>(RolesTable)
    var includes by RolesTable.includes
}