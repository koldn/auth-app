package org.authapp.security

import io.ktor.server.testing.TestApplicationEngine
import io.ktor.server.testing.createTestEnvironment
import org.authapp.ConfigurationProperties
import org.authapp.configureApplication
import org.authapp.configureDi
import org.authapp.database.domain.DomainUser
import org.authapp.database.repository.DataRepository
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.TestInstance
import org.kodein.di.instance
import org.kodein.di.ktor.di

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
open class BaseApplicationTest {
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
}