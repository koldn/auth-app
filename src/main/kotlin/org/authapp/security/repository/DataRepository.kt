package org.authapp.security.repository

interface DataRepository<T> {
    fun findById(id: String): T?
    fun save(entity: T)
}


