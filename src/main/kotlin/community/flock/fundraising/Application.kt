package community.flock.fundraising

import community.flock.fundraising.config.WebMvcConfig
import community.flock.fundraising.config.WebSecurityConfig
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.data.rest.RepositoryRestMvcAutoConfiguration
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import


@Configuration
@SpringBootApplication(exclude = arrayOf(
        RepositoryRestMvcAutoConfiguration::class
))
@Import(ApplicationConfiguration::class,
        WebMvcConfig::class,
        WebSecurityConfig::class)
class Application : SpringBootServletInitializer()

fun main(args: Array<String>) {
    SpringApplication.run(Application::class.java, *args)
}




