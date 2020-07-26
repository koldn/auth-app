package org.authapp.database.repository

import org.authapp.database.DataBaseAccess
import org.authapp.database.domain.DomainUser
import org.authapp.database.domain.UserEntity
import org.authapp.database.domain.UserTable
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.transactions.transaction

class DbUserRepository(private val dbAccess: DataBaseAccess) : DataRepository<DomainUser> {
    override fun findById(id: String): DomainUser? {
        return transaction(dbAccess.database) {
            UserEntity.findById(id)?.let {
                DomainUser(it.id.value, it.password, it.userRole)
            }
        }
    }

    override fun save(entity: DomainUser) {
        transaction(dbAccess.database) {
            val userToUpdate = UserEntity.findById(entity.userName)
            if (null == userToUpdate) {
                UserEntity.new(entity.userName) {
                    userRole = entity.userRole
                    password = entity.password
                }
            } else {
                userToUpdate.password = entity.password
                userToUpdate.userRole = entity.userRole
            }
        }
    }

    override fun deleteAll() {
        transaction(dbAccess.database) {
            UserTable.deleteAll()
        }
    }
}