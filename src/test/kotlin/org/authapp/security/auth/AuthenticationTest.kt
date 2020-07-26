package org.authapp.security.auth

import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.TestApplicationEngine
import io.ktor.server.testing.createTestEnvironment
import io.ktor.server.testing.handleRequest
import org.authapp.ConfigurationProperties
import org.authapp.configureApplication
import org.authapp.configureDi
import org.authapp.database.domain.DomainUser
import org.authapp.database.repository.DataRepository
import org.authapp.security.KPostgresContainer
import org.authapp.security.TestDatabaseProperties
import org.authapp.security.encrypt.PasswordCoder
import org.authapp.security.user.role.SystemRoles
import org.junit.jupiter.api.*
import org.kodein.di.instance
import org.kodein.di.ktor.di
import java.nio.charset.StandardCharsets
import java.util.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AuthenticationTest {
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
            Assertions.assertEquals(HttpStatusCode.Unauthorized, response.status())
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
            Assertions.assertEquals(HttpStatusCode.Unauthorized, response.status())
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