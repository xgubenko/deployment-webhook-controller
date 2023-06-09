package tech.gubenko.controller

import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import tech.gubenko.config.WebhookProperties
import tech.gubenko.entity.Response
import tech.gubenko.service.DeployService

@Controller
class DeploymentWebhookController(val properties: WebhookProperties, val deployService: DeployService) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @PostMapping("/deploy")
    fun test(@RequestHeader() headers: HttpHeaders, @RequestBody payload: String): ResponseEntity<Response> {


        val payloadUser = deployService.getPusherName(payload)
        val payloadRepository = deployService.getRepositoryUrl(payload)
        val payloadBranch = deployService.getRepositoryBranch(payload)
        val computed = deployService.computeSignature(payload)

        if (!headers.get(properties.signatureHeader)!!.get(0).equals(computed)) {
            logger.info("Signature verification failed")
            return ResponseEntity<Response>(Response("Signature verification failed"), HttpStatus.UNAUTHORIZED)
        }
        if (!properties.repositoryUsers.contains(payloadUser)) {
            logger.warn("User unauthorized: {}", payloadUser)
            return ResponseEntity<Response>(Response("User unauthorized"), HttpStatus.UNAUTHORIZED)
        }

        if (!headers.get(properties.eventHeader)!!.get(0).equals("push")) {
            logger.info("Ping successful")
            return ResponseEntity<Response>(Response("Ping successful"), HttpStatus.OK)
        }

        if (!properties.repositoryPath.equals(payloadRepository)) {
            logger.info("Repository with this origin will not be deployed: {}", payloadRepository)
            return ResponseEntity<Response>(
                Response("Repository with this origin will not be deployed"),
                HttpStatus.BAD_REQUEST
            )
        }

        if (!properties.repositoryBranch.equals(payloadBranch)) {
            logger.info("This branch will not be deployed: {}", payloadRepository)
            return ResponseEntity<Response>(
                Response("This branch will not be deployed"),
                HttpStatus.BAD_REQUEST
            )
        }

        deployService.runDeploy()

        return ResponseEntity<Response>(
            Response(
                String.format(
                    "Deploy triggered", payload, headers.get(properties.signatureHeader)
                )
            ), HttpStatus.OK
        )

    }
}