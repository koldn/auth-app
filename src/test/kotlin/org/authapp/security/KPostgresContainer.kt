package org.authapp.security

import org.testcontainers.containers.PostgreSQLContainer

class KPostgresContainer(name: String) : PostgreSQLContainer<KPostgresContainer>(name)