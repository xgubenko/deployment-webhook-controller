package tech.gubenko.controller

import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.commons.codec.digest.HmacUtils
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import tech.gubenko.entity.Response

@Controller
class DeploymentWebhookController {
    private val logger = LoggerFactory.getLogger(javaClass)

    @Value("\${script.path}")
    lateinit var scriptPath: String

    @Value("\${repository.secret}")
    lateinit var repositorySecret: String

    @Value("\${repository.path}")
    lateinit var repositoryPath: String

    @Value("\${repository.users}")
    lateinit var repositoryUsers: List<String>

    @Value("\${repository.headers.signature}")
    lateinit var signatureHeader: String

    @Value("\${repository.headers.event}")
    lateinit var eventHeader: String

    @Value("\${encoding}")
    lateinit var encodingAlgorithm: String

    @PostMapping("/deploy")
    fun test(@RequestHeader() headers: HttpHeaders, @RequestBody payload: String): ResponseEntity<Response> {
        val computed = String.format("%s=%s", encodingAlgorithm, HmacUtils.hmacSha1Hex(repositorySecret, payload))

        val root = ObjectMapper().readTree(payload)
        val payloadUser = root.path("pusher").path("name").textValue()
        val payloadRepository = root.path("repository").path("html_url").textValue()

        if (!headers.get(signatureHeader)!!.equals(computed)) {
            logger.info("Signature verification failed")
            return ResponseEntity<Response>(Response("Signature verification failed"), HttpStatus.UNAUTHORIZED)
        }
        if (!repositoryUsers.contains(payloadUser)) {
            logger.warn("User unauthorized: {}", payloadUser)
            return ResponseEntity<Response>(Response("User unauthorized"), HttpStatus.UNAUTHORIZED)
        }

        if (!headers.get(eventHeader)!!.get(0).equals("push")) {
            logger.info("Ping successful")
            return ResponseEntity<Response>(Response("Ping successful"), HttpStatus.OK)
        }

        if (!repositoryPath.equals(payloadRepository)) {
            logger.info("Repository with this origin will not be deployed: {}", payloadRepository)
            return ResponseEntity<Response>(
                Response("Repository with this origin will not be deployed"),
                HttpStatus.BAD_REQUEST
            )
        }

        logger.info("Deploy started")
        val pb = ProcessBuilder(scriptPath)
        pb.start()
        logger.info("Deploy successful")
        return ResponseEntity<Response>(Response("Deploy successful"), HttpStatus.OK)
    }
}