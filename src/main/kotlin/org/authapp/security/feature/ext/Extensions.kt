package org.authapp.security.feature.ext

import io.ktor.application.ApplicationCall
import io.ktor.application.feature
import io.ktor.routing.*
import org.authapp.security.feature.AuthConstants
import org.authapp.security.feature.Authentication
import org.authapp.security.feature.spi.Principal

fun ApplicationCall.getPrincipal(): Principal {
    return this.attributes[AuthConstants.principalKey]
}

fun Route.authenticate(role: String? = null, handler: Route.() -> Unit): Route {
    val authenticatedRouteWithRole = createChild(object : RouteSelector(RouteSelectorEvaluation.qualityConstant) {
        override fun evaluate(context: RoutingResolveContext, segmentIndex: Int): RouteSelectorEvaluation {
            return RouteSelectorEvaluation.Constant
        }

    })
    application.feature(Authentication).installAuthenticationToPipeLine(authenticatedRouteWithRole, role)
    authenticatedRouteWithRole.handler()
    return authenticatedRouteWithRole
}

