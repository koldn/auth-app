package org.authapp

import org.authapp.security.auth.AuthenticatorCodes
import org.authapp.security.auth.BasicAuthenticator
import org.authapp.security.encrypt.DefaultPasswordCoder
import org.authapp.security.encrypt.PasswordCoder
import org.authapp.security.feature.spi.Authenticator
import org.authapp.security.jwt.JwtAuthenticator
import org.authapp.security.jwt.JwtTokenFactory
import org.authapp.security.jwt.TokenFactory
import org.authapp.security.repository.*
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

fun database() = DI.Module("Database access") {
    bind<DataBaseAccess>() with singleton {
        val configurationProperties by di.instance<ConfigurationProperties>()
        val driverClass = configurationProperties.getProperty("database.driver")
        val jdbcUrl = configurationProperties.getProperty("database.jdbcUrl")
        val userName = configurationProperties.getProperty("database.username")
        val password = configurationProperties.getProperty("database.password")
        val maxActiveConnections = configurationProperties.getProperty("database.maxActiveConnections")
        val access = DataBaseAccess(
                driverClass,
                jdbcUrl,
                userName,
                password,
                maxActiveConnections.toInt()
        )
        DatabaseInitializer(access).initTables()
        return@singleton access
    }
}

fun repositories() = DI.Module("Application repositories") {
    bind<DataRepository<DomainUser>>() with singleton { DbUserRepository(instance()) }
    bind<DataRepository<RoleAggregate>>() with singleton { InMemoryRoleAggregateRepository() }
}