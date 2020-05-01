package community.flock.eco.fundraising.controllers

import community.flock.eco.fundraising.services.MailchimpService
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.ModelAndView

@RestController
@RequestMapping("/api/mailchimp")
@ConditionalOnProperty("flock.fundraising.mailchimp.enabled", havingValue = "true", matchIfMissing = true)
class MailchimpController(
        private val mailchimpService: MailchimpService
) {

    @Value("\${flock.eco.feature.mailchimp.listId:}")
    private lateinit var listId: String

    @RequestMapping("/members")
    fun getMembers(): ModelAndView {
        return ModelAndView("forward:/api/mailchimp/lists/$listId/members");

    }

    @RequestMapping("/interest-categories")
    fun getInterestCategories(): ModelAndView {
        return ModelAndView("forward:/api/mailchimp/lists/$listId/interest-categories")

    }

    @GetMapping("/sync")
    fun sync() {
        mailchimpService.syncGroups()
        mailchimpService.syncMembers()

    }
}
