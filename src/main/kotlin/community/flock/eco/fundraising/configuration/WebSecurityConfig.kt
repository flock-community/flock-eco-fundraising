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
    lateinit var userSecurityService: UserSecurityService

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
            userSecurityService.testLogin(http)
        else
            userSecurityService.googleLogin(http)
    }


}

