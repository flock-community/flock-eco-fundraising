package community.flock.eco.fundraising.data

import community.flock.eco.core.data.LoadData
import community.flock.eco.feature.member.data.MemberLoadData
import community.flock.eco.feature.member.repositories.MemberRepository
import community.flock.eco.feature.payment.data.PaymentLoadData
import community.flock.eco.feature.user.forms.UserAccountPasswordForm
import community.flock.eco.feature.user.model.User
import community.flock.eco.feature.user.model.UserAccount
import community.flock.eco.feature.user.repositories.UserRepository
import community.flock.eco.feature.user.services.UserAccountService
import community.flock.eco.feature.user.services.UserAuthorityService
import community.flock.eco.feature.user.services.UserService
import community.flock.eco.fundraising.model.Donation
import community.flock.eco.fundraising.repositories.DonationRepository
import community.flock.eco.fundraising.services.GenerateTransactionsService
import community.flock.eco.fundraising.services.MemberFieldService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component
import java.time.LocalDate
import javax.annotation.PostConstruct

@Component
@ConditionalOnProperty(
        "flock.fundraising.load-data.enabled",
        "flock.fundraising.load-data.stub")
class UserLoadData(
        private val userService: UserService,
        private val userAccountService: UserAccountService,
        private val userAuthorityService: UserAuthorityService
){

    val data:MutableMap<String, User> = mutableMapOf()

    @PostConstruct
    fun init() {

        data["test"] = userService.findByEmail("test") ?: userAccountService
                .createUserAccountPassword(UserAccountPasswordForm(
               email = "test",
               password = "test",
               authorities = userAuthorityService.allAuthorities()
                       .map { it.toName() }
                       .toSet()
       )).user

    }

}
