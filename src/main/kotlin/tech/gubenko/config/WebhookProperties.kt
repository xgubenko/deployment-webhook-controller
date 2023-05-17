package tech.gubenko.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "webhook")
open class WebhookProperties {
    lateinit var scriptPath: String
    lateinit var secret: String
    lateinit var repositoryPath: String
    lateinit var repositoryUsers: List<String>
    lateinit var signatureHeader: String
    lateinit var eventHeader: String
    lateinit var encoding: String
}