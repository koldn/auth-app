package org.authapp

import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.routing.post
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
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
    routing {
        post("/authenticate") {
            val handler by di().instance<ApplicationCallHandler>(tag = "authHandler")
            handler.handleCall(call)
        }
    }
}