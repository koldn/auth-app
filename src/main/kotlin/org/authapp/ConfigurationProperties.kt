package org.authapp

import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory

class ConfigurationProperties {
    private val config: Config = ConfigFactory.load()

    fun getProperty(path: String): String {
        return config.getString(path)
    }
}