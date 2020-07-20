package org.authapp

import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.response.respondText
import io.ktor.routing.post
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import org.authapp.security.TokenFactory
import org.authapp.security.auth.DefaultUserCredentialsExtractor
import org.authapp.security.feature.Authentication
import org.authapp.security.feature.Authenticator
import org.authapp.security.feature.getPrincipal
import org.kodein.di.allInstances
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
        bind<ConfigurationProperties>() with singleton { ConfigurationProperties() }
    }
    install(Authentication) {
        val availableAuthenticators by di().allInstances<Authenticator>()
        authenticators = availableAuthenticators.toList()
        credentialsExtractor = DefaultUserCredentialsExtractor
    }
    routing {
        post("/authenticate") {
            val token by di().instance<TokenFactory>()
            val userName = call.getPrincipal().userName()
            call.respondText(token.createToken(userName))
        }
    }
}