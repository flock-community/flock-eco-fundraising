package community.flock.eco.fundraising

import community.flock.eco.feature.gcp.runtimeconfig.GcpRuntimeconfigConfiguration
import community.flock.eco.feature.mailchimp.MailchimpConfiguration
import community.flock.eco.feature.member.MemberConfiguration
import community.flock.eco.feature.payment.PaymentConfiguration
import community.flock.eco.feature.user.UserConfiguration
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@Configuration
@EnableJpaRepositories
@EntityScan
@ComponentScan(basePackages = [
    "community.flock.eco.fundraising.service",
    "community.flock.eco.fundraising.controllers"
])
@Import(UserConfiguration::class,
        MemberConfiguration::class,
        PaymentConfiguration::class,
        MailchimpConfiguration::class,
        GcpRuntimeconfigConfiguration::class)
class ApplicationConfiguration