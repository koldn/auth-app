package org.authapp.security.auth

import org.authapp.database.domain.DomainUser
import org.authapp.database.repository.DataRepository
import org.authapp.security.encrypt.PasswordCoder
import org.authapp.security.feature.spi.*
import org.authapp.security.user.PrincipalFactory

class BasicAuthenticator(
        private val userRepository: DataRepository<DomainUser>,
        private val principalFactory: PrincipalFactory,
        private val passwordCoder: PasswordCoder
) : Authenticator {

    data class BasicCredentials(val userName: String, val password: String) : UserCredentials

    override suspend fun authenticate(credentials: UserCredentials): AuthenticationResult {
        if (credentials !is BasicCredentials) {
            return FailedAuthentication("Unsupported credentials")
        }
        val userName = credentials.userName
        val rawPassword = credentials.password
        val domainUser = (userRepository.findById(userName)
                ?: return FailedAuthentication("User $userName not found"))
        if (!passwordCoder.matches(rawPassword, domainUser.password)) {
            return FailedAuthentication("Invalid password")
        }
        return SuccessFullAuthentication(principalFactory.createPrincipal(domainUser))
    }

    override fun code() = AuthenticatorCodes.BASIC
}