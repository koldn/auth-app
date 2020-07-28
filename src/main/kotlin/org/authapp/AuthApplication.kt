package org.authapp

import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.http.HttpStatusCode
import io.ktor.request.receiveParameters
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.routing.post
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import org.authapp.authfeature.Authentication
import org.authapp.authfeature.ext.authenticate
import org.authapp.authfeature.ext.getPrincipal
import org.authapp.authfeature.spi.Authenticator
import org.authapp.database.config.DatabaseProperties
import org.authapp.database.config.DefaultDatabaseProperties
import org.authapp.database.domain.DomainUser
import org.authapp.database.repository.DataRepository
import org.authapp.security.auth.AuthenticatorCodes
import org.authapp.security.auth.DefaultUserCredentialsExtractor
import org.authapp.security.encrypt.PasswordCoder
import org.authapp.security.jwt.TokenFactory
import org.authapp.security.user.role.SystemRoles
import org.kodein.di.bind
import org.kodein.di.instance
import org.kodein.di.ktor.di
import org.kodein.di.singleton


fun main() {
    val configurationProperties = ConfigurationProperties()
    createServer(configurationProperties = ConfigurationProperties(), databaseProperties = DefaultDatabaseProperties(configurationProperties)).start(true)
}


fun createServer(
        port: Int = 8080,
        configurationProperties: ConfigurationProperties,
        databaseProperties: DatabaseProperties
) = embeddedServer(Netty, port) {
    configureDi(configurationProperties, databaseProperties)
    configureApplication()
}

fun Application.configureDi(props: ConfigurationProperties, dbProps: DatabaseProperties) {
    di {
        import(database(dbProps))
        import(repositories())
        import(securityDeps())
        bind<ConfigurationProperties>() with singleton { props }
    }
}

fun Application.configureApplication() {
    install(Authentication) {
        val basicAuthenticator by di().instance<Authenticator>(tag = AuthenticatorCodes.BASIC)
        val tokenAuthenticator by di().instance<Authenticator>(tag = AuthenticatorCodes.TOKEN)
        authenticators = listOf(basicAuthenticator, tokenAuthenticator)
        credentialsExtractor = DefaultUserCredentialsExtractor
    }
    routing {
        post("/register") {
            val parameters = call.receiveParameters()
            val username = parameters["username"]
            val password = parameters["password"]
            if (username.isNullOrEmpty() or password.isNullOrEmpty()) {
                call.respond(HttpStatusCode.BadRequest, "Username or password is not specified")
                return@post
            }
            val userRepository by di().instance<DataRepository<DomainUser>>()
            val passwordCoder by di().instance<PasswordCoder>()

            if (userRepository.findById(username!!) != null) {
                call.respond(HttpStatusCode.BadRequest, "User already exists")
                return@post
            }

            val encoded = passwordCoder.encodePassword(password!!)
            userRepository.save(DomainUser(username, encoded, SystemRoles.USER))
            call.respond("Registered")
        }

        authenticate(AuthenticatorCodes.TOKEN) {
            post("/change_password") {
                val parameters = call.receiveParameters()
                val oldPassword = parameters["oldPassword"]
                if (oldPassword.isNullOrEmpty()) {
                    call.respond(HttpStatusCode.BadRequest, "Old password is not specified")
                    return@post
                }
                val newPassword = parameters["newPassword"]
                if (newPassword.isNullOrEmpty()) {
                    call.respond(HttpStatusCode.BadRequest, "New password is not specified")
                    return@post
                }
                val userName = call.getPrincipal().userName()
                val userRepository by di().instance<DataRepository<DomainUser>>()
                val passwordCoder by di().instance<PasswordCoder>()
                val user = userRepository.findById(userName)!!
                if (!passwordCoder.matches(oldPassword, user.password)) {
                    call.respond(HttpStatusCode.BadRequest, "Passwords do not match")
                    return@post
                }
                user.password = passwordCoder.encodePassword(newPassword)
                userRepository.save(user)
                call.respond("Changed")
            }
        }

        authenticate(AuthenticatorCodes.BASIC) {
            post("/authenticate") {
                val token by di().instance<TokenFactory>()
                val userName = call.getPrincipal().userName()
                call.respondText(token.createToken(userName))
            }
        }

        authenticate(AuthenticatorCodes.TOKEN, SystemRoles.ADMIN) {
            post("/admin_space") {
                call.respondText("Hello admin!")
            }
        }
        authenticate(AuthenticatorCodes.TOKEN, SystemRoles.REVIEWER) {
            post("/reviewer_space") {
                call.respondText("Hello reviewer!")
            }
        }
        authenticate(AuthenticatorCodes.TOKEN, SystemRoles.USER) {
            post("/user_space") {
                call.respondText("Hello user!")
            }
        }
    }
}