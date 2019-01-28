package community.flock.fundraising.controllers

import community.flock.eco.feature.gcp.runtimeconfig.clients.GcpRuntimeConfigurationClient
import community.flock.eco.feature.gcp.runtimeconfig.model.GcpRuntimeVariable
import org.springframework.core.env.Environment
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/system-configuration")
class SystemConfigurationController(
        val environment: Environment,
        val gcpRuntimeConfigurationClient: GcpRuntimeConfigurationClient) {


    @GetMapping("/properties")
    fun properties(): List<GcpRuntimeVariable> {
        val config = "doneasy_local"
        val variables = gcpRuntimeConfigurationClient.getVariablesList(config).variables
        return variables
    }

}