package org.authapp.security.repository

import org.authapp.security.repository.domain.DomainUser
import org.authapp.security.repository.domain.UserEntity
import org.authapp.security.repository.domain.UserTable
import org.jetbrains.exposed.sql.transactions.transaction

class DbUserRepository(private val dbAccess: DataBaseAccess) : DataRepository<DomainUser> {
    override fun findById(id: String): DomainUser? {
        return transaction(dbAccess.database) {
            val userByName = UserEntity.find { UserTable.userName eq id }
            if (userByName.empty()) {
                return@transaction null
            } else {
                val userEntity = userByName.first()
                return@transaction DomainUser(userName = userEntity.userName, password = userEntity.password, roleAggregateId = userEntity.roleAggregate)
            }
        }
    }

    override fun save(entity: DomainUser) {
        transaction(dbAccess.database) {
            UserEntity.new {
                password = entity.password
                userName = entity.userName
                roleAggregate = entity.roleAggregateId
            }
        }
    }
}