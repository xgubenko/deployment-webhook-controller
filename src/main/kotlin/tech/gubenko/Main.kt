package tech.gubenko

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import tech.gubenko.config.WebhookProperties

@SpringBootApplication
@EnableConfigurationProperties(WebhookProperties::class)
open class Main

fun main(args: Array<String>) {
    runApplication<Main>(*args)
}