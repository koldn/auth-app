package org.authapp

import org.authapp.security.TokenFactory
import org.authapp.security.auth.Authenticator
import org.authapp.security.auth.BasicAuthenticator
import org.authapp.security.encrypt.DefaultPasswordCoder
import org.authapp.security.encrypt.PasswordCoder
import org.authapp.security.handlers.AuthenticateRequestHandler
import org.authapp.security.jwt.JwtTokenFactory
import org.authapp.security.user.InMemoryPrincipalLoader
import org.authapp.security.user.PrincipalLoader
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.instance
import org.kodein.di.singleton

fun securityDeps() = DI.Module("Security components", false) {
    //authenticate handler
    bind<ApplicationCallHandler>(tag = "authHandler") with singleton { AuthenticateRequestHandler(instance(tag = "basic"), instance()) }

    //token factory
    bind<TokenFactory>() with singleton {
        val configurationProperties by di.instance<ConfigurationProperties>()
        JwtTokenFactory(configurationProperties.getProperty("jwt.secret"))
    }

    bind<PasswordCoder>() with singleton { DefaultPasswordCoder() }
    bind<PrincipalLoader>() with singleton { InMemoryPrincipalLoader() }
    bind<Authenticator>(tag = "basic") with singleton { BasicAuthenticator(instance(), instance()) }
}