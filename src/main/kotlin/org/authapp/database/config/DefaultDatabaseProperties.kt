package org.authapp.database.config

import org.authapp.ConfigurationProperties

class DefaultDatabaseProperties(private val config: ConfigurationProperties) : DatabaseProperties {
    override fun jdbcUrl(): String = config.getProperty("database.jdbcUrl")

    override fun driver(): String {
        return config.getProperty("database.driver")
    }

    override fun username(): String {
        return config.getProperty("database.username")
    }

    override fun password(): String {
        return config.getProperty("database.password")
    }

    override fun maxActiveConnections(): Int {
        return config.getProperty("database.maxActiveConnections").toInt()
    }
}