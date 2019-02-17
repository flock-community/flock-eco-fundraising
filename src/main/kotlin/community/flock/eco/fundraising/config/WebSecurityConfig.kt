package community.flock.eco.fundraising.config

import community.flock.eco.feature.user.model.User
import community.flock.eco.feature.user.repositories.UserRepository
import community.flock.eco.feature.user.services.UserAuthorityService
import community.flock.eco.fundraising.authorities.DonationsAuthority
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.core.oidc.user.OidcUserAuthority
import org.springframework.security.core.userdetails.User as UserDetail


@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true)
class WebSecurityConfig : WebSecurityConfigurerAdapter() {

    @Autowired
    lateinit var environment: Environment

    @Autowired
    lateinit var userAuthorityService: UserAuthorityService


    @Autowired
    lateinit var userRepository: UserRepository

    override fun configure(http: HttpSecurity) {

        userAuthorityService.addAuthority(DonationsAuthority::class.java)

        http
                .csrf().disable()
        http
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

        http
                .cors()

        if (environment.activeProfiles.contains("local"))
            http.localLogin()
        else
            http.cloudLogin()
    }

    fun HttpSecurity.localLogin(): HttpSecurity {

        val allAuthorities = userAuthorityService
                .allAuthorities()
                .map { it.toName() }
                .map { SimpleGrantedAuthority(it) }
                .plus(SimpleGrantedAuthority("ROLE_USER"))
                .toTypedArray()

        val user = UserDetail.withDefaultPasswordEncoder()
                .username("user")
                .password("password")
                .authorities(*allAuthorities)
                .build()

        this.userDetailsService { user }
        this.formLogin()

        return this
    }

    fun HttpSecurity.cloudLogin(): HttpSecurity {
        this
                .oauth2Login()
                .userInfoEndpoint()
                .userAuthoritiesMapper {
                    val authority = it.first() as OidcUserAuthority
                    val name = authority.attributes.get("name").toString()
                    val email = authority.attributes.get("email").toString()
                    val count = userRepository.count()

                    val allAuthorities = userAuthorityService
                            .allAuthorities()
                            .map { it.toName() }
                            .toSet()

                    val user = userRepository.findByReference(email)
                            .orElseGet {
                                val user = User(
                                        reference = email,
                                        name = name,
                                        email = email,
                                        authorities = if (count == 0L) allAuthorities else setOf()
                                )
                                userRepository.save(user)
                            }

                    user.let {
                        it.authorities
                                .map { SimpleGrantedAuthority(it) }
                                .plus(SimpleGrantedAuthority("ROLE_USER"))
                    }

                }
        return this
    }


}

