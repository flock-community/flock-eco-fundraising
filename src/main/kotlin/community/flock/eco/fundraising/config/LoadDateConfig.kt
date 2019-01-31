package community.flock.eco.fundraising.config

import community.flock.eco.feature.member.data.MemberLoadData
import community.flock.eco.feature.payment.data.PaymentLoadData
import community.flock.eco.feature.user.services.UserAuthorityService
import community.flock.eco.fundraising.data.ExcelLoadData
import community.flock.eco.fundraising.data.StubLoadData
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import javax.annotation.PostConstruct

@Configuration
@ConditionalOnProperty("flock.fundraising.load-data.enabled")
@Import(
        MemberLoadData::class,
        PaymentLoadData::class,
        ExcelLoadData::class)
class LoadDateConfig