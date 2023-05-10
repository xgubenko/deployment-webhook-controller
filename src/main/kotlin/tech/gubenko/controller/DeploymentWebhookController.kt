package tech.gubenko.controller

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody

@Controller
class DeploymentWebhookController {
    private val logger = LoggerFactory.getLogger(javaClass)

    @Value("\${script.path}")
    lateinit var scriptPath: String;

    @Value("\${secret}")
    lateinit var secret: String

    @PostMapping("/deploy")
    fun test(@RequestBody json: String): ResponseEntity<String> {
        logger.info("Deploy started")
        val mapper = jacksonObjectMapper()
        val temp = mapper.readTree(json)
        val requestSecret = temp.at("/hook/config/secret").textValue()
        if (secret.equals(requestSecret)) {
            val pb = ProcessBuilder(scriptPath)
            pb.start()
            logger.info("Deploy successful")
            return ResponseEntity.status(HttpStatus.OK).build()
        } else {
            logger.warn("Deploy failed: secret incorrect")
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build()
        }
    }
}