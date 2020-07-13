package org.authapp

import org.authapp.security.TokenFactory
import org.authapp.security.handlers.AuthenticateRequestHandler
import org.authapp.security.jwt.JwtTokenFactory
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.instance
import org.kodein.di.singleton

fun securityDeps() = DI.Module("Security components", false) {
    //authenticate handler
    bind<ApplicationCallHandler>(tag = "authHandler") with singleton { AuthenticateRequestHandler(instance()) }

    //token factory
    bind<TokenFactory>() with singleton {
        val configurationProperties by di.instance<ConfigurationProperties>()
        JwtTokenFactory(configurationProperties.getProperty("jwt.secret"))
    }
}