package org.authapp

import org.authapp.security.auth.AuthenticatorCodes
import org.authapp.security.auth.BasicAuthenticator
import org.authapp.security.encrypt.DefaultPasswordCoder
import org.authapp.security.encrypt.PasswordCoder
import org.authapp.security.feature.spi.Authenticator
import org.authapp.security.jwt.JwtAuthenticator
import org.authapp.security.jwt.JwtTokenFactory
import org.authapp.security.jwt.TokenFactory
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
        val jwtSecret = configurationProperties.getProperty("jwt.secret")
        val issuer = configurationProperties.getProperty("jwt.app_name")
        JwtTokenFactory(issuer, jwtSecret)
    }

    bind<PasswordCoder>() with singleton { DefaultPasswordCoder() }
    bind<PrincipalLoader>() with singleton { DefaultPrincipalLoader(instance(), instance()) }
    bind<Authenticator>(tag = AuthenticatorCodes.BASIC) with singleton { BasicAuthenticator(instance(), instance()) }
    bind<Authenticator>(tag = AuthenticatorCodes.TOKEN) with singleton {
        val configurationProperties by di.instance<ConfigurationProperties>()
        val jwtSecret = configurationProperties.getProperty("jwt.secret")
        val issuer = configurationProperties.getProperty("jwt.app_name")
        JwtAuthenticator(instance(), issuer, jwtSecret)
    }
}

fun repositories() = DI.Module("Application repositories", true) {
    bind<DataRepository<DomainUser>>() with singleton { InMemoryUserRepository() }
    bind<DataRepository<RoleAggregate>>() with singleton { InMemoryRoleAggregateRepository() }
}