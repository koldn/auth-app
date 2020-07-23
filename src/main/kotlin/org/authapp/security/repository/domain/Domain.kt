package org.authapp.security.repository.domain

class DomainUser(val userName: String, val password: String, val roleAggregateId: String)
class RoleAggregate(val aggregateId: String, val roles: Set<String>)