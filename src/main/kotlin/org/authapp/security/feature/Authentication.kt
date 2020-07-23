package org.authapp.security.feature

import io.ktor.application.ApplicationCallPipeline
import io.ktor.application.ApplicationFeature
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.util.AttributeKey
import io.ktor.util.pipeline.PipelinePhase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import org.authapp.security.feature.ext.getPrincipal
import org.authapp.security.feature.spi.*


object AuthConstants {
    val principalKey = AttributeKey<Principal>("principal")
}

class Authentication(config: Config) {

    private val authPhase = PipelinePhase("auth")
    private val rolesPhase = PipelinePhase("rolesCheck")
    private val authenticators = config.authenticators.toList()
    private val credentialsExtractor = config.credentialsExtractor!!

    class Config {
        var authenticators: List<Authenticator> = listOf()
        var credentialsExtractor: UserCredentialsExtractor? = null
    }

    fun installAuthenticationToPipeLine(pipeline: ApplicationCallPipeline, role: String?) {
        installAuthentication(pipeline)
        if (role != null) {
            pipeline.insertPhaseAfter(authPhase, rolesPhase)
            pipeline.intercept(rolesPhase) {
                if (!call.getPrincipal().hasRole(role)) {
                    call.respond(HttpStatusCode.Unauthorized, "Insufficient role")
                    finish()
                }
            }
        }
    }

    private fun installAuthentication(pipeline: ApplicationCallPipeline) {
        pipeline.insertPhaseAfter(ApplicationCallPipeline.Features, authPhase)
        pipeline.intercept(authPhase) {
            val credentials = credentialsExtractor.extract(call)
            if (credentials == null) {
                call.respond(HttpStatusCode.Unauthorized, "Credentials not specified")
                this.finish()
                return@intercept
            }
            val result = async(Dispatchers.IO) {
                authenticators.forEach {
                    val authenticationResult = it.authenticate(credentials)
                    if (authenticationResult !is UnsupportedAuthenticationCredentials) {
                        return@async authenticationResult
                    }
                }
                return@async FailedAuthentication("Authenticator not found")
            }.await()
            when (result) {
                is SuccessFullAuthentication -> {
                    call.attributes.put(AuthConstants.principalKey, result.principal)
                }
                is FailedAuthentication -> {
                    call.respond(HttpStatusCode.Unauthorized, result.errorMessage)
                    this.finish()
                }
                else -> call.respond(HttpStatusCode.InternalServerError, "This is bug")
            }
        }
    }

    companion object Feature : ApplicationFeature<ApplicationCallPipeline, Config, Authentication> {

        override val key: AttributeKey<Authentication> = AttributeKey("Authentication")

        override fun install(pipeline: ApplicationCallPipeline, configure: Config.() -> Unit): Authentication {
            return Authentication(Config().apply(configure))
        }
    }
}

