package org.authapp.authfeature.spi

interface Principal {
    fun userName(): String
    fun hasRole(role: String): Boolean
}