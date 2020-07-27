package org.authapp.security

import io.ktor.http.*
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.setBody
import org.authapp.database.domain.DomainUser
import org.authapp.database.repository.DataRepository
import org.authapp.security.encrypt.PasswordCoder
import org.authapp.security.user.role.SystemRoles
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.kodein.di.instance
import org.kodein.di.ktor.di
import java.nio.charset.StandardCharsets
import java.util.*

class ChangePasswordTest : BaseApplicationTest() {

    @Test
    fun testPasswordChange() {
        val userRepository by engine.application.di().instance<DataRepository<DomainUser>>()
        val passwordCoder by engine.application.di().instance<PasswordCoder>()
        userRepository.save(DomainUser("test", passwordCoder.encodePassword("12345"), SystemRoles.USER))

        var credentials = Base64.getEncoder().encodeToString("test:12345".toByteArray(StandardCharsets.UTF_8))

        val token: String
        with(engine.handleRequest(HttpMethod.Post, "/authenticate") {
            addHeader(HttpHeaders.Authorization, "Basic $credentials")
        }) {
            token = response.content!!
        }
        with(engine.handleRequest(HttpMethod.Post, "/change_password") {
            addHeader(HttpHeaders.Authorization, "Bearer $token")
            addHeader(HttpHeaders.ContentType, ContentType.Application.FormUrlEncoded.toString())
            setBody(listOf("oldPassword" to "12345", "newPassword" to "supahSecret").formUrlEncode())
        }) {
            Assertions.assertTrue(response.status()!!.isSuccess()) {
                "Expected to successfully change password"
            }
        }
        with(engine.handleRequest(HttpMethod.Post, "/authenticate") {
            addHeader(HttpHeaders.Authorization, "Basic $credentials")
        }) {
            Assertions.assertEquals(HttpStatusCode.Unauthorized, response.status()) {
                "Expected to be unauthorized"
            }
            Assertions.assertEquals("Invalid password", response.content) {
                "Unexpected message"
            }
        }
        credentials = Base64.getEncoder().encodeToString("test:supahSecret".toByteArray(StandardCharsets.UTF_8))
        with(engine.handleRequest(HttpMethod.Post, "/authenticate") {
            addHeader(HttpHeaders.Authorization, "Basic $credentials")
        }) {
            Assertions.assertTrue(response.status()!!.isSuccess()) {
                "Expected to get success status code "
            }
        }
    }

    @Test
    fun testPasswordsDoNotMatch() {
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
        with(engine.handleRequest(HttpMethod.Post, "/change_password") {
            addHeader(HttpHeaders.Authorization, "Bearer $token")
            addHeader(HttpHeaders.ContentType, ContentType.Application.FormUrlEncoded.toString())
            setBody(listOf("oldPassword" to "1234", "newPassword" to "supahSecret").formUrlEncode())
        }) {
            Assertions.assertEquals(HttpStatusCode.BadRequest, response.status()) {
                "Expected to be bad request"
            }
            Assertions.assertEquals("Passwords do not match", response.content) {
                "Unexpected message"
            }
        }
    }

    @Test
    fun testEmptyOldPassword() {
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
        with(engine.handleRequest(HttpMethod.Post, "/change_password") {
            addHeader(HttpHeaders.Authorization, "Bearer $token")
            addHeader(HttpHeaders.ContentType, ContentType.Application.FormUrlEncoded.toString())
            setBody(listOf("oldPassword" to "", "newPassword" to "supahSecret").formUrlEncode())
        }) {
            Assertions.assertEquals(HttpStatusCode.BadRequest, response.status()) {
                "Expected to be bad request"
            }
            Assertions.assertEquals("Old password is not specified", response.content) {
                "Unexpected message"
            }
        }
    }

    @Test
    fun testNullOldPassword() {
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
        with(engine.handleRequest(HttpMethod.Post, "/change_password") {
            addHeader(HttpHeaders.Authorization, "Bearer $token")
            addHeader(HttpHeaders.ContentType, ContentType.Application.FormUrlEncoded.toString())
            setBody(listOf("newPassword" to "supahSecret").formUrlEncode())
        }) {
            Assertions.assertEquals(HttpStatusCode.BadRequest, response.status()) {
                "Expected to be bad request"
            }
            Assertions.assertEquals("Old password is not specified", response.content) {
                "Unexpected message"
            }
        }
    }

    @Test
    fun testEmptyNewPassword() {
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
        with(engine.handleRequest(HttpMethod.Post, "/change_password") {
            addHeader(HttpHeaders.Authorization, "Bearer $token")
            addHeader(HttpHeaders.ContentType, ContentType.Application.FormUrlEncoded.toString())
            setBody(listOf("oldPassword" to "supahSecret", "newPassword" to "").formUrlEncode())
        }) {
            Assertions.assertEquals(HttpStatusCode.BadRequest, response.status()) {
                "Expected to be bad request"
            }
            Assertions.assertEquals("New password is not specified", response.content) {
                "Unexpected message"
            }
        }
    }

    @Test
    fun testNullNewPassword() {
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
        with(engine.handleRequest(HttpMethod.Post, "/change_password") {
            addHeader(HttpHeaders.Authorization, "Bearer $token")
            addHeader(HttpHeaders.ContentType, ContentType.Application.FormUrlEncoded.toString())
            setBody(listOf("oldPassword" to "supahSecret").formUrlEncode())
        }) {
            Assertions.assertEquals(HttpStatusCode.BadRequest, response.status()) {
                "Expected to be bad request"
            }
            Assertions.assertEquals("New password is not specified", response.content) {
                "Unexpected message"
            }
        }
    }
}