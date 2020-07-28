package org.authapp.security.user

import org.authapp.authfeature.spi.Principal
import org.authapp.database.domain.DomainUser
import org.authapp.database.domain.RoleAggregate
import org.authapp.database.repository.DataRepository
import org.authapp.security.user.role.UserRole

class DefaultPrincipalFactory(private val rolesRepository: DataRepository<UserRole>) : PrincipalFactory {
    override fun createPrincipal(user: DomainUser): Principal {
        return UserPrincipal(user.userName, buildRoleAggregate(user.userRole))
    }

    private fun buildRoleAggregate(fromRole: String): RoleAggregate {
        val rolesSet = mutableSetOf<String>()
        val role = rolesRepository.findById(fromRole)
        if (role != null) {
            var includesRole = role.includes
            rolesSet.add(role.name)
            while (includesRole != null) {
                includesRole = rolesRepository.findById(includesRole)?.apply {
                    rolesSet.add(this.name)
                    includesRole = this.includes
                }?.includes
            }
        }
        return RoleAggregate(rolesSet.toSet())
    }
}