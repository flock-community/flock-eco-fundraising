package community.flock.eco.fundraising.config

import community.flock.eco.feature.user.model.User
import community.flock.eco.feature.user.repositories.UserRepository
import community.flock.eco.feature.user.services.UserAuthorityService
import community.flock.eco.fundraising.authorities.DonationsAuthority
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.security.oauth2.client.EnableOAuth2Sso
import org.springframework.boot.autoconfigure.security.oauth2.resource.AuthoritiesExtractor
import org.springframework.boot.autoconfigure.security.oauth2.resource.PrincipalExtractor
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.core.authority.SimpleGrantedAuthority


@Configuration
@EnableOAuth2Sso
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true)
@Profile("!local")
class WebSecurityConfig : WebSecurityConfigurerAdapter() {

    @Autowired
    lateinit var userAuthorityService: UserAuthorityService


    override fun configure(http: HttpSecurity) {

        userAuthorityService.addAuthority(DonationsAuthority::class.java)

        http
                .csrf().disable()
                .authorizeRequests()
                .antMatchers("/configuration").permitAll()
                .antMatchers("/login").permitAll()
                .antMatchers("/_ah/**").permitAll()
                .antMatchers("/actuator/**").permitAll()
                .antMatchers(HttpMethod.GET, "/tasks/**").permitAll()
                .antMatchers(HttpMethod.GET, "/api/donations/donate").permitAll()
                .antMatchers(HttpMethod.POST, "/api/donations/donate").permitAll()
                .antMatchers(HttpMethod.POST, "/api/payment/buckaroo/**").permitAll()
                .antMatchers(HttpMethod.GET, "/api/mailchimp/webhook").permitAll()
                .antMatchers(HttpMethod.POST, "/api/mailchimp/webhook").permitAll()
                .anyRequest().hasRole("USER")
                .and()
                .cors()


    }

    @Bean
    fun principalExtractor(userRepository: UserRepository): PrincipalExtractor {
        return PrincipalExtractor {
            val reference = it.get("email").toString()

            val count = userRepository.count()
            val user = userRepository.findByReference(reference)

            if (!user.isPresent) {
                val allAuthority = userAuthorityService
                        .allAuthorities()
                        .map { it.toName() }
                        .toSet()
                userRepository.save(User(
                        reference = reference,
                        name = it.get("name").toString(),
                        email = it.get("email").toString(),
                        authorities = if (count == 0L) allAuthority else setOf()
                ))
            } else {
                user.get()
            }
        }
    }

    @Bean
    fun authoritiesExtractor(userRepository: UserRepository): AuthoritiesExtractor {

        return AuthoritiesExtractor {
            val reference = it.get("email").toString()
            userRepository.findByReference(reference)
                    .filter { it.authorities.isNotEmpty() }
                    .map { it.authorities.map { SimpleGrantedAuthority(it) } + listOf(SimpleGrantedAuthority("ROLE_USER")) }
                    .orElse(listOf())
        }
    }


}

