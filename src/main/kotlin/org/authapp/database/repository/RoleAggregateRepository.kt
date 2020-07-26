package org.authapp.database.repository

import org.authapp.database.domain.Role
import org.authapp.database.domain.RoleAggregate
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.concurrent.ConcurrentHashMap

//TODO computed aggregate should be computed elsewhere
class RoleAggregateRepository(dataBase: Database) : DataRepository<RoleAggregate> {

    private val existingRoles = ConcurrentHashMap<String, Role>()
    private val computedRoleAggregates = ConcurrentHashMap<String, RoleAggregate>()

    init {
        transaction(dataBase) {
            Role.all().forEach {
                existingRoles[it.id.value] = it
            }
        }
    }

    override fun findById(id: String): RoleAggregate? {
        if (id.isEmpty()) {
            return null
        }
        return computedRoleAggregates.computeIfAbsent(id) {
            val role = existingRoles[id]!!
            var parent = role.includes
            val principleRoles = mutableSetOf<String>()
            principleRoles.add(role.id.value)
            while (parent != null) {
                val parentRole = existingRoles[parent]!!
                principleRoles.add(parentRole.id.value)
                parent = parentRole.includes
            }
            return@computeIfAbsent RoleAggregate(principleRoles.toSet())
        }
    }

    override fun save(entity: RoleAggregate) = throw UnsupportedOperationException("New roles are unsupported yet")

    override fun deleteAll() = throw UnsupportedOperationException("New roles are unsupported yet")
}

