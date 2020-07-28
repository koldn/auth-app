package org.authapp.database.config

import io.ktor.config.ApplicationConfig
import io.ktor.util.KtorExperimentalAPI

@KtorExperimentalAPI
class DefaultDatabaseProperties(private val config: ApplicationConfig) : DatabaseProperties {
    override fun jdbcUrl(): String = config.property("database.jdbcUrl").getString()

    override fun driver(): String {
        return config.property("database.driver").getString()
    }

    override fun username(): String {
        return config.property("database.username").getString()
    }

    override fun password(): String {
        return config.property("database.password").getString()
    }

    override fun maxActiveConnections(): Int {
        return config.property("database.maxActiveConnections").getString().toInt()
    }
}