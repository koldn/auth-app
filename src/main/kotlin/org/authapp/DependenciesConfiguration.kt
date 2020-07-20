package org.authapp

import org.authapp.security.TokenFactory
import org.authapp.security.auth.BasicAuthenticator
import org.authapp.security.encrypt.DefaultPasswordCoder
import org.authapp.security.encrypt.PasswordCoder
import org.authapp.security.feature.Authenticator
import org.authapp.security.jwt.JwtTokenFactory
import org.authapp.security.user.InMemoryPrincipalLoader
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
    bind<PrincipalLoader>() with singleton { InMemoryPrincipalLoader() }
    bind<Authenticator>() with singleton { BasicAuthenticator(instance(), instance()) }
}