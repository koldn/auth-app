package org.authapp.authfeature.ext

import io.ktor.application.ApplicationCall
import io.ktor.application.feature
import io.ktor.routing.*
import org.authapp.authfeature.AuthConstants
import org.authapp.authfeature.Authentication
import org.authapp.authfeature.spi.Principal

fun ApplicationCall.getPrincipal(): Principal {
    return this.attributes[AuthConstants.principalKey]
}

fun Route.authenticate(authenticatorCode: String, role: String? = null, handler: Route.() -> Unit): Route {
    val authenticatedRouteWithRole = createChild(object : RouteSelector(RouteSelectorEvaluation.qualityConstant) {
        override fun evaluate(context: RoutingResolveContext, segmentIndex: Int): RouteSelectorEvaluation {
            return RouteSelectorEvaluation.Constant
        }
    })
    application.feature(Authentication).installAuthenticationToPipeLine(authenticatedRouteWithRole, authenticatorCode, role)
    authenticatedRouteWithRole.handler()
    return authenticatedRouteWithRole
}

