package community.flock.fundraising.config

import community.flock.eco.feature.member.data.MemberLoadData
import community.flock.eco.feature.payment.data.PaymentLoadData
import community.flock.fundraising.data.ExcelLoadData
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import

@Configuration
@ConditionalOnProperty("flock.fundraising.load-data.enabled")
@Import(
        MemberLoadData::class,
        PaymentLoadData::class,
        ExcelLoadData::class)
class LoadDateConfig