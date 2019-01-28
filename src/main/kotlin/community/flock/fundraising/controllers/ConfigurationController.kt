package community.flock.fundraising.controllers

import community.flock.eco.feature.user.services.UserAuthorityService
import org.springframework.beans.factory.annotation.Value
import org.springframework.cloud.context.config.annotation.RefreshScope
import org.springframework.security.oauth2.provider.OAuth2Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.security.Principal


@RefreshScope
@RestController
@RequestMapping("/configuration")
class ConfigurationController(
        val userAuthorityService: UserAuthorityService) {

    @Value("\${flock.fundraising.name}")
    lateinit var name: String

    data class Configuration(
            val applicationName: String,
            val isLoggedIn: Boolean,
            val authorities: List<String>
    )

    @GetMapping
    fun index(principal: Principal?): Configuration {
        return Configuration(
                applicationName = name,
                isLoggedIn = principal != null,
                authorities = when (principal) {
                    is OAuth2Authentication -> principal.authorities
                            .map { it.authority }
                    else -> listOf()
                }
        )
    }
}