package org.authapp.database

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.sql.Database

class DataBaseAccess(
        private val driver: String,
        private val url: String,
        private val userName: String,
        private val pass: String,
        private val intMaxConnections: Int) {
    private val poolConfiguration = HikariConfig().apply {
        this.driverClassName = driver
        this.jdbcUrl = url
        this.username = userName
        this.password = pass
        this.maximumPoolSize = intMaxConnections
    }
    val database: Database by lazy {
        Database.connect(HikariDataSource(poolConfiguration))
    }
}