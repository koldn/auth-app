package org.authapp.security.user

import org.authapp.database.domain.DomainUser
import org.authapp.database.domain.RoleAggregate
import org.authapp.database.repository.DataRepository
import org.authapp.security.feature.spi.Principal
import org.authapp.security.user.role.UserRole

class DefaultPrincipalLoader(
        private val userRepository: DataRepository<DomainUser>,
        private val rolesRepository: DataRepository<UserRole>
) : PrincipalLoader {
    override fun loadPrincipal(userName: String): Principal? {
        return userRepository.findById(userName)?.let {
            UserPrincipal(it.userName, it.password, buildRoleAggregate(it.userRole))
        }
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