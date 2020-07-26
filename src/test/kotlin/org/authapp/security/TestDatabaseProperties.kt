package org.authapp.security

import org.authapp.database.config.DatabaseProperties

class TestDatabaseProperties(private val container: KPostgresContainer) : DatabaseProperties {
    override fun jdbcUrl(): String {
        return container.jdbcUrl
    }

    override fun driver(): String {
        return container.driverClassName
    }

    override fun username(): String {
        return container.username
    }

    override fun password(): String {
        return container.password
    }

    override fun maxActiveConnections(): Int {
        return 8
    }
}