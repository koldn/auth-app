package org.authapp

import org.authapp.security.TokenFactory
import org.authapp.security.auth.BasicAuthenticator
import org.authapp.security.encrypt.DefaultPasswordCoder
import org.authapp.security.encrypt.PasswordCoder
import org.authapp.security.feature.Authenticator
import org.authapp.security.jwt.JwtTokenFactory
import org.authapp.security.repository.DataRepository
import org.authapp.security.repository.InMemoryRoleAggregateRepository
import org.authapp.security.repository.InMemoryUserRepository
import org.authapp.security.repository.domain.DomainUser
import org.authapp.security.repository.domain.RoleAggregate
import org.authapp.security.user.DefaultPrincipalLoader
import org.authapp.security.user.PrincipalLoader
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.instance
import org.kodein.di.singleton

fun securityDeps() = DI.Module("Security components", false) {
    //token factory
    bind<TokenFactory>() with singleton {
        val configurationProperties by di.instance<ConfigurationProperties>()
        JwtTokenFactory(configurationProperties.getProperty("jwt.secret"))
    }

    bind<PasswordCoder>() with singleton { DefaultPasswordCoder() }
    bind<PrincipalLoader>() with singleton { DefaultPrincipalLoader(instance(), instance()) }
    bind<Authenticator>() with singleton { BasicAuthenticator(instance(), instance()) }
}

fun repositories() = DI.Module("Application repositories", true) {
    bind<DataRepository<DomainUser>>() with singleton { InMemoryUserRepository() }
    bind<DataRepository<RoleAggregate>>() with singleton { InMemoryRoleAggregateRepository() }
}