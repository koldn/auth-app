package org.authapp

import io.ktor.application.call
import io.ktor.routing.get
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import org.kodein.di.bind
import org.kodein.di.instance
import org.kodein.di.ktor.di
import org.kodein.di.singleton


fun main() {
    embeddedServer(Netty, port = 8080) {
        di {
            import(securityDeps())
            bind<ConfigurationProperties>() with singleton { ConfigurationProperties() }
        }
        routing {
            get("/authenticate") {
                val handler by di().instance<ApplicationCallHandler>(tag = "authHandler")
                handler.handleCall(call)
            }
        }
    }.start(wait = true)
}