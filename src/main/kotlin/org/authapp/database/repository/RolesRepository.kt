package org.authapp.database.repository

import org.authapp.database.domain.Role
import org.authapp.database.domain.RolesTable
import org.authapp.security.user.role.UserRole
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.concurrent.ConcurrentHashMap

class RolesRepository(private val database: Database) : DataRepository<UserRole> {

    private val rolesCache = ConcurrentHashMap<String, UserRole>()

    override fun findById(id: String): UserRole? {
        val cachedValue = rolesCache[id]
        //this could be a computeIfAbsent, but javadoc state that mapping function should be quick and simple,
        //what cannot be said about db interaction
        //and if running on JDK < 9, https://bugs.openjdk.java.net/browse/JDK-8161372 could be a problem
        if (cachedValue == null) {
            val fromDb = transaction(database) { Role.findById(id) }?.let { UserRole(it.id.value, it.includes) }
            return if (fromDb != null) {
                rolesCache.putIfAbsent(id, fromDb) ?: fromDb
            } else {
                null
            }
        }
        return cachedValue
    }

    override fun save(entity: UserRole) {
        transaction(database) {
            val role = Role.findById(entity.name)
            if (role != null) {
                role.includes = entity.includes
            } else {
                Role.new(entity.name) {
                    includes = entity.includes
                }
            }
        }
        //Some kind of TransactionSync#afterCompletion
        rolesCache.remove(entity.name)
    }

    override fun deleteAll() {
        transaction(database) {
            RolesTable.deleteAll()
        }
        rolesCache.clear()
    }

}