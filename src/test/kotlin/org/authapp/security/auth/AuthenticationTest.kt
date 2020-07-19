package org.authapp.security.auth

import io.ktor.application.Application
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.withTestApplication
import org.authapp.configureApplication
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
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
        val password = Base64.getEncoder().encodeToString("test:12345".toByteArray(StandardCharsets.UTF_8))
        with(handleRequest(HttpMethod.Post, "/authenticate") {
            addHeader(HttpHeaders.Authorization, "Basic $password")
        }) {
            Assertions.assertEquals(HttpStatusCode.OK, response.status())
            Assertions.assertNotNull(response.content)
        }
    }
}