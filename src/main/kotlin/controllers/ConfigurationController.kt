package community.flock.eco.fundraising.controllers

import community.flock.eco.feature.user.model.User
import community.flock.eco.feature.user.repositories.UserRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.cloud.context.config.annotation.RefreshScope
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.security.Principal
import java.util.*


@RefreshScope
@RestController
@RequestMapping("/configuration")
class ConfigurationController(
        val userRepository: UserRepository) {

    @Value("\${flock.fundraising.name}")
    lateinit var name: String

    data class Configuration(
            val applicationName: String,
            val isLoggedIn: Boolean,
            val authorities: List<String>
    )

    @GetMapping
    fun index(principal: Principal?): Configuration {
        userRepository.findAll()
                .filter { it.code == null }
                .map { user ->
                    User(
                            id = user.id,
                            code = UUID.randomUUID().toString(),
                            email = user.email,
                            name = user.name,
                            enabled = true,
                            authorities = user.authorities)
                }
                .apply { userRepository.saveAll(this) }

        return Configuration(
                applicationName = name,
                isLoggedIn = principal != null,
                authorities = when (principal) {
                    is OAuth2AuthenticationToken -> principal.authorities
                            .map { it.authority }
                    is UsernamePasswordAuthenticationToken -> principal.authorities
                            .map { it.authority }
                    else -> listOf()
                }
        )
    }
}
