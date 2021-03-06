package community.flock.eco.fundraising.configuration

import community.flock.eco.feature.user.services.UserAuthorityService
import community.flock.eco.feature.user.services.UserSecurityService
import community.flock.eco.fundraising.authorities.DonationsAuthority
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true)
class WebSecurityConfig : WebSecurityConfigurerAdapter() {

    @Autowired
    lateinit var environment: Environment

    @Autowired
    lateinit var userAuthorityService: UserAuthorityService

    @Autowired
    lateinit var userSecurityService: UserSecurityService

    override fun configure(http: HttpSecurity) {

        userAuthorityService.addAuthority(DonationsAuthority::class.java)

        http
            .headers().frameOptions().sameOrigin()
        http
            .csrf().disable()
        http
            .authorizeRequests()
            .antMatchers("/index.html").permitAll()
            .antMatchers("/main.*.js").permitAll()
            .antMatchers("/donataion.js").permitAll()
            .antMatchers("/donataion.html").permitAll()

            .antMatchers("/configuration").permitAll()
            .antMatchers("/login").permitAll()
            .antMatchers("/_ah/**").permitAll()
            .antMatchers("/h2-console/**").permitAll()
            .antMatchers("/actuator/**").permitAll()

            .antMatchers(HttpMethod.GET, "/tasks/**").permitAll()
            .antMatchers(HttpMethod.GET, "/api/donations/donate").permitAll()
            .antMatchers(HttpMethod.POST, "/api/donations/donate").permitAll()
            .antMatchers(HttpMethod.POST, "/api/payment/buckaroo/**").permitAll()
            .antMatchers(HttpMethod.GET, "/api/mailchimp/webhook").permitAll()
            .antMatchers(HttpMethod.POST, "/api/mailchimp/webhook").permitAll()
            .anyRequest().authenticated()

        http
            .cors()

        when {
            environment.activeProfiles.isEmpty() -> userSecurityService.testLogin(http)
            "develop" in environment.activeProfiles -> userSecurityService.testLogin(http)
            else -> userSecurityService.googleLogin(http)
        }
    }
}
