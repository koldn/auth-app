package org.authapp.security.auth

import io.ktor.application.Application
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.withTestApplication
import org.authapp.configureApplication
import org.authapp.security.encrypt.PasswordCoder
import org.authapp.security.repository.DataRepository
import org.authapp.security.repository.domain.DomainUser
import org.authapp.security.repository.domain.SystemDefinedAggregate
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.kodein.di.instance
import org.kodein.di.ktor.di
import java.nio.charset.StandardCharsets
import java.util.*

class AuthenticationTest {
    @Test
    fun `Request without authorization header should be responded with 'Unauthorized' status code`(): Unit = withTestApplication(Application::configureApplication) {
        with(handleRequest(HttpMethod.Post, "/authenticate")) {
            Assertions.assertEquals(HttpStatusCode.Unauthorized, response.status())
        }
    }

    @Test
    fun `Unauthorized when user is not found`(): Unit = withTestApplication(Application::configureApplication) {
        val password = Base64.getEncoder().encodeToString("test:test1".toByteArray(StandardCharsets.UTF_8))
        with(handleRequest(HttpMethod.Post, "/authenticate") {
            addHeader(HttpHeaders.Authorization, "Basic $password")
        }) {
            Assertions.assertEquals(HttpStatusCode.Unauthorized, response.status())
        }
    }

    @Test
    fun `Token should be handled to found user`(): Unit = withTestApplication(Application::configureApplication) {
        val userRepository by application.di().instance<DataRepository<DomainUser>>()
        val passwordCoder by application.di().instance<PasswordCoder>()
        userRepository.save(DomainUser("test", passwordCoder.encodePassword("12345"), ""))
        val credentials = Base64.getEncoder().encodeToString("test:12345".toByteArray(StandardCharsets.UTF_8))
        with(handleRequest(HttpMethod.Post, "/authenticate") {
            addHeader(HttpHeaders.Authorization, "Basic $credentials")
        }) {
            Assertions.assertEquals(HttpStatusCode.OK, response.status())
            Assertions.assertNotNull(response.content)
        }
    }

    @Test
    fun `Unauthorized on admin space with reviewer role`(): Unit = withTestApplication(Application::configureApplication) {
        val userRepository by application.di().instance<DataRepository<DomainUser>>()
        val passwordCoder by application.di().instance<PasswordCoder>()
        userRepository.save(DomainUser("test", passwordCoder.encodePassword("12345"), SystemDefinedAggregate.REVIEWER_ROLE_AGGREGATE.aggregateId))
        val credentials = Base64.getEncoder().encodeToString("test:12345".toByteArray(StandardCharsets.UTF_8))

        val token: String
        with(handleRequest(HttpMethod.Post, "/authenticate") {
            addHeader(HttpHeaders.Authorization, "Basic $credentials")
        }) {
            token = response.content!!
        }

        with(handleRequest(HttpMethod.Post, "/admin_space") {
            addHeader(HttpHeaders.Authorization, "Bearer $token")
        }) {
            Assertions.assertEquals(HttpStatusCode.Unauthorized, response.status())
            Assertions.assertEquals("Insufficient role", response.content)
        }
    }

    @Test
    fun `Unauthorized on reviewer space with user role`(): Unit = withTestApplication(Application::configureApplication) {
        val userRepository by application.di().instance<DataRepository<DomainUser>>()
        val passwordCoder by application.di().instance<PasswordCoder>()
        userRepository.save(DomainUser("test", passwordCoder.encodePassword("12345"), SystemDefinedAggregate.USER_ROLE_AGGREGATE.aggregateId))
        val credentials = Base64.getEncoder().encodeToString("test:12345".toByteArray(StandardCharsets.UTF_8))

        val token: String
        with(handleRequest(HttpMethod.Post, "/authenticate") {
            addHeader(HttpHeaders.Authorization, "Basic $credentials")
        }) {
            token = response.content!!
        }

        with(handleRequest(HttpMethod.Post, "/reviewer_space") {
            addHeader(HttpHeaders.Authorization, "Bearer $token")
        }) {
            Assertions.assertEquals(HttpStatusCode.Unauthorized, response.status())
            Assertions.assertEquals("Insufficient role", response.content)
        }
    }

    @Test
    fun `Higher role has access`(): Unit = withTestApplication(Application::configureApplication) {
        val userRepository by application.di().instance<DataRepository<DomainUser>>()
        val passwordCoder by application.di().instance<PasswordCoder>()
        userRepository.save(DomainUser("test", passwordCoder.encodePassword("12345"), SystemDefinedAggregate.ADMIN_ROLE_AGGREGATE.aggregateId))
        val credentials = Base64.getEncoder().encodeToString("test:12345".toByteArray(StandardCharsets.UTF_8))

        val token: String
        with(handleRequest(HttpMethod.Post, "/authenticate") {
            addHeader(HttpHeaders.Authorization, "Basic $credentials")
        }) {
            token = response.content!!
        }

        with(handleRequest(HttpMethod.Post, "/reviewer_space") {
            addHeader(HttpHeaders.Authorization, "Bearer $token")
        }) {
            Assertions.assertEquals("Hello reviewer!", response.content)
        }
    }

    @Test
    fun `User role has access to user space`(): Unit = withTestApplication(Application::configureApplication) {
        val userRepository by application.di().instance<DataRepository<DomainUser>>()
        val passwordCoder by application.di().instance<PasswordCoder>()
        userRepository.save(DomainUser("test", passwordCoder.encodePassword("12345"), SystemDefinedAggregate.USER_ROLE_AGGREGATE.aggregateId))
        val credentials = Base64.getEncoder().encodeToString("test:12345".toByteArray(StandardCharsets.UTF_8))

        val token: String
        with(handleRequest(HttpMethod.Post, "/authenticate") {
            addHeader(HttpHeaders.Authorization, "Basic $credentials")
        }) {
            token = response.content!!
        }

        with(handleRequest(HttpMethod.Post, "/user_space") {
            addHeader(HttpHeaders.Authorization, "Bearer $token")
        }) {
            Assertions.assertEquals("Hello user!", response.content)
        }
    }
}