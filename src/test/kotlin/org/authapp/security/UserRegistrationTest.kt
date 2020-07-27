package org.authapp.security

import io.ktor.http.*
import io.ktor.server.testing.TestApplicationEngine
import io.ktor.server.testing.createTestEnvironment
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.setBody
import org.authapp.ConfigurationProperties
import org.authapp.configureApplication
import org.authapp.configureDi
import org.authapp.database.domain.DomainUser
import org.authapp.database.repository.DataRepository
import org.junit.jupiter.api.*
import org.kodein.di.instance
import org.kodein.di.ktor.di
import java.nio.charset.StandardCharsets
import java.util.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UserRegistrationTest {
    lateinit var engine: TestApplicationEngine

    companion object {
        val container: KPostgresContainer = KPostgresContainer("postgres:12")
                .withDatabaseName("test")
                .withUsername("test")
                .withPassword("testPassword")
    }

    @BeforeAll
    fun startServer() {
        container.start()
        val environment = createTestEnvironment()
        val testApplicationEngine = TestApplicationEngine(environment)
        testApplicationEngine.start()
        val configurationProperties = ConfigurationProperties()
        val databaseConf = TestDatabaseProperties(container)
        testApplicationEngine.application.configureDi(configurationProperties, databaseConf)
        testApplicationEngine.application.configureApplication()
        engine = testApplicationEngine
    }

    @AfterEach
    internal fun clearRepositories() {
        val users by engine.application.di().instance<DataRepository<DomainUser>>()
        users.deleteAll()
    }

    @AfterAll
    internal fun stopServer() {
        engine.stop(0L, 0L)
        container.stop()
    }

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