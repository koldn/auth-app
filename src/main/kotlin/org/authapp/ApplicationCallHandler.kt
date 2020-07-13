package org.authapp

import io.ktor.application.ApplicationCall

/**
 * An interface for handling incoming ApplicationCall
 */
interface ApplicationCallHandler {
    suspend fun handleCall(applicationCall: ApplicationCall)
}