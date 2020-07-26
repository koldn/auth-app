package org.authapp.security.repository.domain

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Column

class DomainUser(val userName: String, val password: String, val roleAggregateId: String)
class RoleAggregate(val aggregateId: String, val roles: Set<String>)

//Exposed
//Tables
object UserTable : IntIdTable(name = "tbl_users", columnName = "user_id") {
    val userName: Column<String> = varchar("username", 255).uniqueIndex("idx_username")
    val password: Column<String> = varchar("password", length = 255)
    val roleAggregate: Column<String> = varchar("role_aggr", length = 255)
}

object RoleAggregateTable : IntIdTable(name = "tbl_role_aggregate", columnName = "aggregate_id") {
    val name: Column<String> = varchar("aggregate_name", length = 255).uniqueIndex("idx_aggregate_name")
    val roles = reference("roles", RoleTable)
}

object RoleTable : IntIdTable(name = "tbl_role", columnName = "role_id") {
    val name: Column<String> = varchar("role_name", length = 255)
}

//Entities
class UserEntity(entityID: EntityID<Int>) : IntEntity(entityID) {
    companion object : IntEntityClass<UserEntity>(UserTable)

    var userName by UserTable.userName
    var password by UserTable.password
    var roleAggregate by UserTable.roleAggregate
}