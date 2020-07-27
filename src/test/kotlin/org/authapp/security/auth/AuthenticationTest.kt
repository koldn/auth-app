package org.authapp.security.auth

import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.handleRequest
import org.authapp.database.domain.DomainUser
import org.authapp.database.repository.DataRepository
import org.authapp.security.BaseApplicationTest
import org.authapp.security.encrypt.PasswordCoder
import org.authapp.security.user.role.SystemRoles
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.kodein.di.instance
import org.kodein.di.ktor.di
import java.nio.charset.StandardCharsets
import java.util.*

class AuthenticationTest : BaseApplicationTest() {

    @Test
    fun `Request without authorization header should be responded with 'Unauthorized' status code`() {
        with(engine.handleRequest(HttpMethod.Post, "/authenticate")) {
            Assertions.assertEquals(HttpStatusCode.Unauthorized, response.status())
        }
    }

    @Test
    fun `Unauthorized when user is not found`() {
        val password = Base64.getEncoder().encodeToString("test:test1".toByteArray(StandardCharsets.UTF_8))
        with(engine.handleRequest(HttpMethod.Post, "/authenticate") {
            addHeader(HttpHeaders.Authorization, "Basic $password")
        }) {
            Assertions.assertEquals(HttpStatusCode.Unauthorized, response.status())
        }
    }

    @Test
    fun `Token should be handled to found user`() {
        val userRepository by engine.application.di().instance<DataRepository<DomainUser>>()
        val passwordCoder by engine.application.di().instance<PasswordCoder>()
        userRepository.save(DomainUser("test", passwordCoder.encodePassword("12345"), ""))
        val credentials = Base64.getEncoder().encodeToString("test:12345".toByteArray(StandardCharsets.UTF_8))
        with(engine.handleRequest(HttpMethod.Post, "/authenticate") {
            addHeader(HttpHeaders.Authorization, "Basic $credentials")
        }) {
            Assertions.assertEquals(HttpStatusCode.OK, response.status())
            Assertions.assertNotNull(response.content)
        }
    }

    @Test
    fun `Unauthorized on admin space with reviewer role`() {
        val userRepository by engine.application.di().instance<DataRepository<DomainUser>>()
        val passwordCoder by engine.application.di().instance<PasswordCoder>()
        userRepository.save(DomainUser("test", passwordCoder.encodePassword("12345"), SystemRoles.REVIEWER))
        val credentials = Base64.getEncoder().encodeToString("test:12345".toByteArray(StandardCharsets.UTF_8))

        val token: String
        with(engine.handleRequest(HttpMethod.Post, "/authenticate") {
            addHeader(HttpHeaders.Authorization, "Basic $credentials")
        }) {
            token = response.content!!
        }

        with(engine.handleRequest(HttpMethod.Post, "/admin_space") {
            addHeader(HttpHeaders.Authorization, "Bearer $token")
        }) {
            Assertions.assertEquals(HttpStatusCode.Forbidden, response.status())
            Assertions.assertEquals("Insufficient role", response.content)
        }
    }

    @Test
    fun `Unauthorized on reviewer space with user role`() {
        val userRepository by engine.application.di().instance<DataRepository<DomainUser>>()
        val passwordCoder by engine.application.di().instance<PasswordCoder>()
        userRepository.save(DomainUser("test", passwordCoder.encodePassword("12345"), SystemRoles.USER))
        val credentials = Base64.getEncoder().encodeToString("test:12345".toByteArray(StandardCharsets.UTF_8))

        val token: String
        with(engine.handleRequest(HttpMethod.Post, "/authenticate") {
            addHeader(HttpHeaders.Authorization, "Basic $credentials")
        }) {
            token = response.content!!
        }

        with(engine.handleRequest(HttpMethod.Post, "/reviewer_space") {
            addHeader(HttpHeaders.Authorization, "Bearer $token")
        }) {
            Assertions.assertEquals(HttpStatusCode.Forbidden, response.status())
            Assertions.assertEquals("Insufficient role", response.content)
        }
    }

    @Test
    fun `Higher role has access`() {
        val userRepository by engine.application.di().instance<DataRepository<DomainUser>>()
        val passwordCoder by engine.application.di().instance<PasswordCoder>()
        userRepository.save(DomainUser("test", passwordCoder.encodePassword("12345"), SystemRoles.ADMIN))
        val credentials = Base64.getEncoder().encodeToString("test:12345".toByteArray(StandardCharsets.UTF_8))

        val token: String
        with(engine.handleRequest(HttpMethod.Post, "/authenticate") {
            addHeader(HttpHeaders.Authorization, "Basic $credentials")
        }) {
            token = response.content!!
        }

        with(engine.handleRequest(HttpMethod.Post, "/reviewer_space") {
            addHeader(HttpHeaders.Authorization, "Bearer $token")
        }) {
            Assertions.assertEquals("Hello reviewer!", response.content)
        }
    }

    @Test
    fun `User role has access to user space`() {
        val userRepository by engine.application.di().instance<DataRepository<DomainUser>>()
        val passwordCoder by engine.application.di().instance<PasswordCoder>()
        userRepository.save(DomainUser("test", passwordCoder.encodePassword("12345"), SystemRoles.USER))
        val credentials = Base64.getEncoder().encodeToString("test:12345".toByteArray(StandardCharsets.UTF_8))

        val token: String
        with(engine.handleRequest(HttpMethod.Post, "/authenticate") {
            addHeader(HttpHeaders.Authorization, "Basic $credentials")
        }) {
            token = response.content!!
        }

        with(engine.handleRequest(HttpMethod.Post, "/user_space") {
            addHeader(HttpHeaders.Authorization, "Bearer $token")
        }) {
            Assertions.assertEquals("Hello user!", response.content)
        }
    }
}