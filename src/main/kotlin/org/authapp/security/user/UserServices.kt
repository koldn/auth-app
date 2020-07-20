package org.authapp.security.user

import org.authapp.security.feature.Principal
import java.util.concurrent.ConcurrentHashMap

class UserPrincipal(private val userName: String, private val password: String) : Principal {
    override fun userName() = userName
    override fun password() = password
}

interface PrincipalLoader {
    fun loadPrincipal(userName: String): Principal?
}

class InMemoryPrincipalLoader : PrincipalLoader {
    private val repository = ConcurrentHashMap<String, String>()

    init {
        //test user
        repository["test"] = "QbQHlnmKIJE3thL3wHPNZQ==$\$GRi/UdiEWK1F+A3MYzmPNROGrelIdBTxTB/Zk7KDXqCRUf2qQKb+o2j2H2i72uunnwrQRiHUkSZGx/4ZDnO08g=="
    }

    override fun loadPrincipal(userName: String): Principal? {
        return repository[userName]?.let { UserPrincipal(userName, it) }

    }
}