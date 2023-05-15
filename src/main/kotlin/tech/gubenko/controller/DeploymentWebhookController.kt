package tech.gubenko.controller

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
    lateinit var scriptPath: String;

    @Value("\${repository.secret}")
    lateinit var repositorySecret: String

    @Value("\${repository.path}")
    lateinit var repositoryPath: String

    @Value("\${repository.branch}")
    lateinit var repositoryBranch: String

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
        val user = "test"
        if (!headers.get(signatureHeader)!!.equals(computed)) {
            logger.info("Signature verification failed")
            return ResponseEntity<Response>(Response("Signature verification failed"), HttpStatus.UNAUTHORIZED)
        }

        if (!headers.get(eventHeader)!!.equals("push")) {
            logger.info("ping successful")
            return ResponseEntity<Response>(Response("Ping successful"), HttpStatus.OK)
        }

        if (!repositoryUsers.contains(user)) {
            logger.warn("User unauthorized")
            return ResponseEntity<Response>(Response("User unauthorized"), HttpStatus.UNAUTHORIZED)
        }

        logger.info("Deploy started")
        val pb = ProcessBuilder(scriptPath)
        pb.start()
        logger.info("Deploy successful")
        return ResponseEntity<Response>(Response("Deploy successful"), HttpStatus.OK)
    }
}