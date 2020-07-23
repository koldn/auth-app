package org.authapp

import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.response.respondText
import io.ktor.routing.post
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import org.authapp.security.auth.AuthenticatorCodes
import org.authapp.security.auth.DefaultUserCredentialsExtractor
import org.authapp.security.feature.Authentication
import org.authapp.security.feature.ext.authenticate
import org.authapp.security.feature.ext.getPrincipal
import org.authapp.security.feature.spi.Authenticator
import org.authapp.security.jwt.TokenFactory
import org.authapp.security.user.role.SystemDefinedRoles
import org.kodein.di.bind
import org.kodein.di.instance
import org.kodein.di.ktor.di
import org.kodein.di.singleton


fun main() {
    embeddedServer(Netty, port = 8080) {
        configureApplication()
    }.start(true)
}

fun Application.configureApplication() {
    di {
        import(securityDeps())
        import(repositories())
        bind<ConfigurationProperties>() with singleton { ConfigurationProperties() }
    }
    install(Authentication) {
        val basicAuthenticator by di().instance<Authenticator>(tag = AuthenticatorCodes.BASIC)
        val tokenAuthenticator by di().instance<Authenticator>(tag = AuthenticatorCodes.TOKEN)
        authenticators = listOf(basicAuthenticator, tokenAuthenticator)
        credentialsExtractor = DefaultUserCredentialsExtractor
    }
    routing {
        authenticate(AuthenticatorCodes.BASIC) {
            post("/authenticate") {
                val token by di().instance<TokenFactory>()
                val userName = call.getPrincipal().userName()
                call.respondText(token.createToken(userName))
            }
        }
        authenticate(AuthenticatorCodes.TOKEN, SystemDefinedRoles.ADMIN) {
            post("/admin_space") {
                call.respondText("Hello admin!")
            }
        }
        authenticate(AuthenticatorCodes.TOKEN, SystemDefinedRoles.REVIEWER) {
            post("/reviewer_space") {
                call.respondText("Hello reviewer!")
            }
        }
        authenticate(AuthenticatorCodes.TOKEN, SystemDefinedRoles.USER) {
            post("/user_space") {
                call.respondText("Hello user!")
            }
        }
    }
}