package org.authapp.security.feature.spi

interface Principal {
    fun userName(): String
    fun hasRole(role: String): Boolean
}