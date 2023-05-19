package tech.gubenko.service

import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.commons.codec.digest.HmacUtils
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import tech.gubenko.config.WebhookProperties

@Service
class DeployService(val properties: WebhookProperties) {
    private val logger = LoggerFactory.getLogger(javaClass)

    fun runDeploy() {
        logger.info("Deploy started")
        val pb = ProcessBuilder(properties.scriptPath)
        pb.start()
        logger.info("Deploy successful")
    }

    fun computeSignature(payload: String): String {
        return String.format("%s=%s", properties.encoding, HmacUtils.hmacSha1Hex(properties.secret, payload))
    }

    fun getPusherName(payload: String): String {
        val root = ObjectMapper().readTree(payload)
        return root.path("pusher").path("name").textValue()
    }

    fun getRepositoryUrl(payload: String): String {
        val root = ObjectMapper().readTree(payload)
        return root.path("repository").path("html_url").textValue()
    }

    fun getRepositoryBranch(payload: String): String {
        val root = ObjectMapper().readTree(payload)
        return root.path("ref").textValue()
    }
}