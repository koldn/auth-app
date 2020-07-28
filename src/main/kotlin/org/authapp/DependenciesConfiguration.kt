package org.authapp

import io.ktor.config.ApplicationConfig
import io.ktor.util.KtorExperimentalAPI
import org.authapp.authfeature.spi.Authenticator
import org.authapp.database.DataBaseAccess
import org.authapp.database.DatabaseInitializer
import org.authapp.database.config.DatabaseProperties
import org.authapp.database.domain.DomainUser
import org.authapp.database.repository.DataRepository
import org.authapp.database.repository.DbUserRepository
import org.authapp.database.repository.RolesRepository
import org.authapp.security.auth.AuthenticatorCodes
import org.authapp.security.auth.BasicAuthenticator
import org.authapp.security.encrypt.DefaultPasswordCoder
import org.authapp.security.encrypt.PasswordCoder
import org.authapp.security.jwt.JwtAuthenticator
import org.authapp.security.jwt.JwtTokenFactory
import org.authapp.security.jwt.TokenFactory
import org.authapp.security.user.DefaultPrincipalFactory
import org.authapp.security.user.PrincipalFactory
import org.authapp.security.user.role.UserRole
import org.jetbrains.exposed.sql.transactions.transaction
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.instance
import org.kodein.di.singleton

@KtorExperimentalAPI
fun securityDeps() = DI.Module("Security components", false) {
    //token factory
    bind<TokenFactory>() with singleton {
        val configurationProperties by di.instance<ApplicationConfig>()
        val jwtSecret = configurationProperties.property("jwt.secret").getString()
        val issuer = configurationProperties.property("jwt.app_name").getString()
        JwtTokenFactory(issuer, jwtSecret)
    }

    bind<PasswordCoder>() with singleton { DefaultPasswordCoder() }
    bind<PrincipalFactory>() with singleton { DefaultPrincipalFactory(instance()) }
    bind<Authenticator>(tag = AuthenticatorCodes.BASIC) with singleton {
        BasicAuthenticator(instance(), instance(), instance())
    }
    bind<Authenticator>(tag = AuthenticatorCodes.TOKEN) with singleton {
        val configurationProperties by di.instance<ApplicationConfig>()
        val jwtSecret = configurationProperties.property("jwt.secret").getString()
        val issuer = configurationProperties.property("jwt.app_name").getString()
        JwtAuthenticator(instance(), instance(), issuer, jwtSecret)
    }
}

fun database(dbProps: DatabaseProperties) = DI.Module("Database access") {
    bind<DataBaseAccess>() with singleton {
        val access = DataBaseAccess(
                dbProps.driver(),
                dbProps.jdbcUrl(),
                dbProps.username(),
                dbProps.password(),
                dbProps.maxActiveConnections()
        )
        transaction(access.database) {
            DatabaseInitializer.initTables()
            DatabaseInitializer.initializeSystemRoles()
        }
        access
    }
}

fun repositories() = DI.Module("Application repositories") {
    bind<DataRepository<DomainUser>>() with singleton { DbUserRepository(instance()) }
    bind<DataRepository<UserRole>>() with singleton {
        val dbAccess by di.instance<DataBaseAccess>()
        RolesRepository(dbAccess.database)
    }
}