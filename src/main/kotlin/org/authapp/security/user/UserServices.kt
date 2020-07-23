package org.authapp.security.user

import org.authapp.security.feature.Principal
import org.authapp.security.repository.DataRepository
import org.authapp.security.repository.domain.DomainUser
import org.authapp.security.repository.domain.RoleAggregate

class UserPrincipal(private val userName: String, private val password: String, private val roleAggregate: RoleAggregate) : Principal {
    override fun userName() = userName
    override fun password() = password

    override fun hasRole(role: String): Boolean = roleAggregate.roles.contains(role)
}

interface PrincipalLoader {
    fun loadPrincipal(userName: String): Principal?
}

class DefaultPrincipalLoader(
        private val dataRepository: DataRepository<DomainUser>,
        private val aggregatesRepo: DataRepository<RoleAggregate>
) : PrincipalLoader {
    override fun loadPrincipal(userName: String): Principal? {
        return dataRepository.findById(userName)?.let {
            val roleAggregate = aggregatesRepo.findById(it.roleAggregateId) ?: RoleAggregate("", setOf())
            UserPrincipal(it.userName, it.password, roleAggregate)
        }
    }
}