package org.authapp.security.user

import org.authapp.database.domain.DomainUser
import org.authapp.database.domain.RoleAggregate
import org.authapp.database.repository.DataRepository
import org.authapp.security.feature.spi.Principal

class DefaultPrincipalLoader(
        private val dataRepository: DataRepository<DomainUser>,
        private val aggregatesRepo: DataRepository<RoleAggregate>
) : PrincipalLoader {
    override fun loadPrincipal(userName: String): Principal? {
        return dataRepository.findById(userName)?.let {
            val roleAggregate = aggregatesRepo.findById(it.userRole) ?: RoleAggregate(setOf())
            UserPrincipal(it.userName, it.password, roleAggregate)
        }
    }
}