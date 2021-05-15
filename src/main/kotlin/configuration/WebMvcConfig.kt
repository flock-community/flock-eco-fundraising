package community.flock.eco.fundraising.configuration

import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.ClassPathResource
import org.springframework.core.io.Resource
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import org.springframework.web.servlet.resource.PathResourceResolver
import java.io.IOException

class WebMvcConfig : WebMvcConfigurer {

    @Value("\${flock.fundraising.donations.cors.allowedOrigins:@null}")
    lateinit var cors: String

    override fun addResourceHandlers(registry: ResourceHandlerRegistry) {

        registry.addResourceHandler("/**")
            .addResourceLocations("classpath:/static/")
            .resourceChain(true)
            .addResolver(object : PathResourceResolver() {
                @Throws(IOException::class)
                override fun getResource(resourcePath: String, location: Resource): Resource {
                    val requestedResource = location.createRelative(resourcePath)
                    return if (requestedResource.exists() && requestedResource.isReadable())
                        requestedResource
                    else
                        ClassPathResource("/static/index.html")
                }
            })
    }

    override fun addCorsMappings(registry: CorsRegistry) {
        registry.addMapping("/api/donations/donate")
            .allowedOrigins(*cors.split(",").toTypedArray())
            .allowedMethods("GET", "POST")
            .allowedHeaders("*")
            .allowCredentials(true)
            .maxAge(3600)
    }
}
