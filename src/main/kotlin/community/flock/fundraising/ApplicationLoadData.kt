package community.flock.fundraising

import community.flock.eco.feature.user.repositories.UserRepository
import community.flock.fundraising.data.ExcelLoadData
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.WebApplicationType
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.context.annotation.Import

@EnableAutoConfiguration(exclude = [WebMvcAutoConfiguration::class])
@Import(ApplicationConfiguration::class)
class ApplicationLoadData : CommandLineRunner {

    @Autowired
    lateinit var userRepository: UserRepository

    @Autowired
    lateinit var excelLoadData: ExcelLoadData

    //access command line arguments
    @Throws(Exception::class)
    override fun run(vararg args: String) {
        if (userRepository.count() == 0L) {
            excelLoadData.load()
        }
    }

    companion object {

        @Throws(Exception::class)
        @JvmStatic
        fun main(args: Array<String>) {

            SpringApplicationBuilder(ApplicationLoadData::class.java)
                    .web(WebApplicationType.NONE)
                    .build()
                    .run(*args)

        }
    }
}


