package community.flock.eco.fundraising.configuration

import community.flock.eco.feature.member.develop.data.MemberLoadData
import community.flock.eco.feature.payment.data.PaymentLoadData
import community.flock.eco.fundraising.data.ExcelLoadData
import community.flock.eco.fundraising.data.UserLoadData
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import

@Configuration
@ConditionalOnProperty("flock.fundraising.load-data.enabled")
@Import(
    MemberLoadData::class,
    PaymentLoadData::class,
    ExcelLoadData::class,
    UserLoadData::class
)
class LoadDateConfig
