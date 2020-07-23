package org.authapp.security.repository

import org.authapp.security.repository.domain.RoleAggregate
import org.authapp.security.repository.domain.SystemDefinedAggregate
import java.util.concurrent.ConcurrentHashMap

class InMemoryRoleAggregateRepository : DataRepository<RoleAggregate> {
    private val repo = ConcurrentHashMap<String, RoleAggregate>()

    init {
        repo[SystemDefinedAggregate.USER_ROLE_AGGREGATE.aggregateId] = SystemDefinedAggregate.USER_ROLE_AGGREGATE
        repo[SystemDefinedAggregate.REVIEWER_ROLE_AGGREGATE.aggregateId] = SystemDefinedAggregate.REVIEWER_ROLE_AGGREGATE
        repo[SystemDefinedAggregate.ADMIN_ROLE_AGGREGATE.aggregateId] = SystemDefinedAggregate.ADMIN_ROLE_AGGREGATE
    }

    override fun findById(id: String): RoleAggregate? {
        return repo[id]
    }

    override fun save(entity: RoleAggregate) {
        repo[entity.aggregateId] = entity
    }
}