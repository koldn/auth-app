package org.authapp.security.repository

import org.authapp.security.repository.domain.DomainUser
import java.util.concurrent.ConcurrentHashMap

class InMemoryUserRepository : DataRepository<DomainUser> {
    private val repo = ConcurrentHashMap<String, DomainUser>()

    override fun findById(id: String): DomainUser? {
        return repo[id]
    }

    override fun save(entity: DomainUser) {
        repo[entity.userName] = entity
    }
}