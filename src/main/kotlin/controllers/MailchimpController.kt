package community.flock.eco.fundraising.controllers

import community.flock.eco.feature.mailchimp.clients.MailchimpClient
import community.flock.eco.feature.mailchimp.events.MailchimpWebhookEvent
import community.flock.eco.feature.mailchimp.events.MailchimpWebhookEventType
import community.flock.eco.feature.mailchimp.model.MailchimpMember
import community.flock.eco.feature.mailchimp.model.MailchimpMemberStatus
import community.flock.eco.feature.member.controllers.CreateMemberEvent
import community.flock.eco.feature.member.controllers.MemberEvent
import community.flock.eco.feature.member.controllers.UpdateMemberEvent
import community.flock.eco.feature.member.model.Member
import community.flock.eco.feature.member.repositories.MemberGroupRepository
import community.flock.eco.feature.member.repositories.MemberRepository
import community.flock.eco.fundraising.services.MailchimpService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.event.EventListener
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.ModelAndView
import javax.annotation.PostConstruct

@RestController
@RequestMapping("/api/mailchimp")
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
