package org.authapp

import io.ktor.client.HttpClient
import io.ktor.client.features.defaultRequest
import io.ktor.client.request.*
import io.ktor.client.request.forms.submitForm
import io.ktor.http.HttpHeaders
import io.ktor.http.parametersOf
import io.ktor.server.engine.ApplicationEngine
import kotlinx.coroutines.*
import org.authapp.security.KPostgresContainer
import org.authapp.security.TestDatabaseProperties
import org.junit.jupiter.api.*
import java.net.ServerSocket
import java.nio.charset.StandardCharsets
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import kotlin.random.Random

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ConcurrentFlowTest {
    private var port: Int = 0
    private lateinit var server: ApplicationEngine

    companion object {
        val charPool: List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')
        val container: KPostgresContainer = KPostgresContainer("postgres:12")
                .withDatabaseName("test")
                .withUsername("test")
                .withPassword("testPassword")
    }

    @BeforeAll
    fun setUp() {
        container.start()
        port = ServerSocket(port).use { it.localPort }
        server = createServer(port, TestDatabaseProperties(container))
        server.start(false)
    }

    @AfterAll
    fun stopServices() {
        server.stop(0L, 0L)
        container.stop()
    }

    @Test
    fun testConcurrentFlow() = runBlocking {
        val defaultPassword = "super_password_1!"
        val httpClient = HttpClient() {
            defaultRequest {
                host = "localhost"
                port = this@ConcurrentFlowTest.port
            }
        }

        val users = generateUserNames()
        val userRequestsCounts = ConcurrentHashMap<String, AtomicInteger>()
        users.map { userName ->
            launch(Dispatchers.IO) {
                val regJob = launch(Dispatchers.IO) {
                    val parameters = parametersOf("username" to listOf(userName), "password" to listOf(defaultPassword))
                    httpClient.submitForm<String>(parameters, false) {
                        url("/register")
                    }
                    updateUserRequestCounter(userRequestsCounts, userName)
                }
                regJob.join()

                val token = withContext(Dispatchers.IO) {
                    val response = httpClient.post<String>("/authenticate") {
                        header(HttpHeaders.Authorization, "Basic ${encodeCredentials(userName, defaultPassword)}")
                    }
                    updateUserRequestCounter(userRequestsCounts, userName)
                    return@withContext response
                }

                launch(Dispatchers.IO) {
                    httpClient.post<String>("/user_space") {
                        header(HttpHeaders.Authorization, "Bearer $token")
                    }
                    updateUserRequestCounter(userRequestsCounts, userName)
                }.join()
                val newPassword = generateRandomString()
                launch(Dispatchers.IO) {
                    val parameters = parametersOf(
                            "oldPassword" to listOf(defaultPassword),
                            "newPassword" to listOf(newPassword)
                    )
                    httpClient.submitForm<String>(parameters, false) {
                        url("/change_password")
                        header(HttpHeaders.Authorization, "Bearer $token")
                    }
                    updateUserRequestCounter(userRequestsCounts, userName)
                }.join()

                val newToken = withContext(Dispatchers.IO) {
                    val response = httpClient.post<String>("/authenticate") {
                        header(HttpHeaders.Authorization, "Basic ${encodeCredentials(userName, newPassword)}")
                    }
                    updateUserRequestCounter(userRequestsCounts, userName)
                    return@withContext response
                }
                launch(Dispatchers.IO) {
                    httpClient.post<String>("/user_space") {
                        header(HttpHeaders.Authorization, "Bearer $newToken")
                    }
                    updateUserRequestCounter(userRequestsCounts, userName)
                }.join()
            }
        }.joinAll()
        Assertions.assertEquals(userRequestsCounts.size, users.size)
        users.forEach { userName ->
            Assertions.assertNotNull(userRequestsCounts[userName]) { "Expected to have request count data for $userName" }
            Assertions.assertEquals(6, userRequestsCounts[userName]!!.get()) {
                "Unexpected request count for $userName"
            }
        }
    }

    private fun encodeCredentials(userName: String, defaultPassword: String): String? {
        return Base64.getEncoder().encodeToString(
                "$userName:$defaultPassword".toByteArray(StandardCharsets.UTF_8)
        )
    }

    private fun updateUserRequestCounter(userRequestsCounts: ConcurrentHashMap<String, AtomicInteger>, userName: String) {
        userRequestsCounts.computeIfAbsent(userName) { AtomicInteger() }
        val counter = userRequestsCounts[userName]
        counter!!.incrementAndGet()
        userRequestsCounts[userName] = counter
    }

    private fun generateUserNames(): List<String> {
        return (1..Random.nextInt(100, 150)).map { generateRandomString() }.toList()
    }

    private fun generateRandomString(): String {
        return (1..10).map { Random.nextInt(0, charPool.size) }
                .map(charPool::get)
                .joinToString("")
    }
}