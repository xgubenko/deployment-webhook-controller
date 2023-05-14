package tech.gubenko.controller

import org.apache.commons.codec.digest.HmacUtils
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader

@Controller
class DeploymentWebhookController {
    private val logger = LoggerFactory.getLogger(javaClass)

    @Value("\${script.path}")
    lateinit var scriptPath: String;

    @Value("\${secret}")
    lateinit var secret: String

    @PostMapping("/deploy")
    fun test(
        @RequestHeader("X-Hub-Signature") signature: String,
        @RequestBody payload: String
    ): ResponseEntity<String> {

        val computed = String.format("sha1=%s", HmacUtils.hmacSha1Hex(secret, payload))
        if (signature.equals(computed)) {
            logger.info("Deploy started")
            val pb = ProcessBuilder(scriptPath)
            pb.start()
            logger.info("Deploy successful")
            return ResponseEntity<String>(payload, HttpStatus.OK)
        } else {
            return ResponseEntity<String>(
                computed,
                HttpStatus.UNAUTHORIZED
            )
        }
    }
}