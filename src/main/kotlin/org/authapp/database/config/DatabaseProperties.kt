package org.authapp.database.config

interface DatabaseProperties {
    fun jdbcUrl(): String
    fun driver(): String
    fun username(): String
    fun password(): String
    fun maxActiveConnections(): Int
}