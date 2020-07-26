package org.authapp.database.repository

interface DataRepository<T> {
    fun findById(id: String): T?
    fun save(entity: T)
    fun deleteAll()
}


