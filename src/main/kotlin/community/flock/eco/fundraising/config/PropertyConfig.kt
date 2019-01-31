package community.flock.eco.fundraising.config

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment
import javax.annotation.PostConstruct

@Configuration
class PropertyConfig {

    @Autowired
    lateinit var environment: Environment

    val properties = arrayOf(
            "flock.fundraising.name",
            "flock.fundraising.donations.generate.dayOfMonth",
            "flock.fundraising.donations.collection.dayOfMonth"
    )

    @PostConstruct
    fun init() {
        properties.map {
            val value = environment.getProperty(it)
            println(value)
        }

    }
}