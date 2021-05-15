package community.flock.eco.fundraising.data

import community.flock.eco.feature.user.forms.UserAccountPasswordForm
import community.flock.eco.feature.user.model.User
import community.flock.eco.feature.user.services.UserAccountService
import community.flock.eco.feature.user.services.UserAuthorityService
import community.flock.eco.feature.user.services.UserService
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component
import javax.annotation.PostConstruct

@Component
@ConditionalOnProperty(
    "flock.fundraising.load-data.enabled",
    "flock.fundraising.load-data.stub"
)
class UserLoadData(
    private val userService: UserService,
    private val userAccountService: UserAccountService,
    private val userAuthorityService: UserAuthorityService
) {

    val data: MutableMap<String, User> = mutableMapOf()

    @PostConstruct
    fun init() {

        data["test"] = userService.findByEmail("test") ?: userAccountService
            .createUserAccountPassword(
                UserAccountPasswordForm(
                    email = "test",
                    password = "test",
                    authorities = userAuthorityService.allAuthorities()
                        .map { it.toName() }
                        .toSet()
                )
            ).user
    }
}
