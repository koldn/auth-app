package org.authapp.security

import io.ktor.http.*
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.setBody
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.nio.charset.StandardCharsets
import java.util.*

class UserRegistrationTest : BaseApplicationTest() {

    @Test
    fun testUserRegistration() {
        with(engine.handleRequest(HttpMethod.Post, "/register") {
            addHeader(HttpHeaders.ContentType, ContentType.Application.FormUrlEncoded.toString())
            setBody(listOf("username" to "test", "password" to "12345").formUrlEncode())
        }) {
            Assertions.assertTrue(response.status()!!.isSuccess())
        }
    }

    @Test
    fun `Bad request if username is null`() {
        with(engine.handleRequest(HttpMethod.Post, "/register") {
            addHeader(HttpHeaders.ContentType, ContentType.Application.FormUrlEncoded.toString())
            setBody(listOf("username" to null, "password" to "12345").formUrlEncode())
        }) {
            Assertions.assertEquals(HttpStatusCode.BadRequest, response.status()!!)
            Assertions.assertEquals("Username or password is not specified", response.content)
        }
    }

    @Test
    fun `Bad request if username is empty`() {
        with(engine.handleRequest(HttpMethod.Post, "/register") {
            addHeader(HttpHeaders.ContentType, ContentType.Application.FormUrlEncoded.toString())
            setBody(listOf("username" to "", "password" to "12345").formUrlEncode())
        }) {
            Assertions.assertEquals(HttpStatusCode.BadRequest, response.status()!!)
            Assertions.assertEquals("Username or password is not specified", response.content)
        }
    }

    @Test
    fun `Bad request if password is null`() {
        with(engine.handleRequest(HttpMethod.Post, "/register") {
            addHeader(HttpHeaders.ContentType, ContentType.Application.FormUrlEncoded.toString())
            setBody(listOf("username" to "test", "password" to null).formUrlEncode())
        }) {
            Assertions.assertEquals(HttpStatusCode.BadRequest, response.status()!!)
            Assertions.assertEquals("Username or password is not specified", response.content)
        }
    }

    @Test
    fun `Bad request if password is empty`() {
        with(engine.handleRequest(HttpMethod.Post, "/register") {
            addHeader(HttpHeaders.ContentType, ContentType.Application.FormUrlEncoded.toString())
            setBody(listOf("username" to "test", "password" to "").formUrlEncode())
        }) {
            Assertions.assertEquals(HttpStatusCode.BadRequest, response.status()!!)
            Assertions.assertEquals("Username or password is not specified", response.content)
        }
    }

    @Test
    fun `Bad request if no data`() {
        with(engine.handleRequest(HttpMethod.Post, "/register") {
            addHeader(HttpHeaders.ContentType, ContentType.Application.FormUrlEncoded.toString())
        }) {
            Assertions.assertEquals(HttpStatusCode.BadRequest, response.status()!!)
            Assertions.assertEquals("Username or password is not specified", response.content)
        }
    }

    @Test
    fun `Bad request if already exists`() {
        engine.handleRequest(HttpMethod.Post, "/register") {
            addHeader(HttpHeaders.ContentType, ContentType.Application.FormUrlEncoded.toString())
            setBody(listOf("username" to "test", "password" to "12345").formUrlEncode())
        }
        with(engine.handleRequest(HttpMethod.Post, "/register") {
            addHeader(HttpHeaders.ContentType, ContentType.Application.FormUrlEncoded.toString())
            setBody(listOf("username" to "test", "password" to "12345").formUrlEncode())
        }) {
            Assertions.assertEquals(HttpStatusCode.BadRequest, response.status()!!)
            Assertions.assertEquals("User already exists", response.content)
        }
    }

    @Test
    fun `User can be authenticated after registration`() {
        engine.handleRequest(HttpMethod.Post, "/register") {
            addHeader(HttpHeaders.ContentType, ContentType.Application.FormUrlEncoded.toString())
            setBody(listOf("username" to "test", "password" to "12345").formUrlEncode())
        }
        val token: String
        with(engine.handleRequest(HttpMethod.Post, "/authenticate") {
            val credentials = Base64.getEncoder().encodeToString("test:12345".toByteArray(StandardCharsets.UTF_8))
            addHeader(HttpHeaders.Authorization, "Basic $credentials")
        }) {
            Assertions.assertTrue(response.status()!!.isSuccess())
            token = response.content!!
        }
        with(engine.handleRequest(HttpMethod.Post, "/user_space") {
            addHeader(HttpHeaders.Authorization, "Bearer $token")
        }) {
            Assertions.assertEquals("Hello user!", response.content)
        }
    }
}