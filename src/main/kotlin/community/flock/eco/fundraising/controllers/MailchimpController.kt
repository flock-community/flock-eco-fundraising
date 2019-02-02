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
import org.slf4j.LoggerFactory
import org.springframework.context.event.EventListener
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/mailchimp")
class MailchimpController(
        private val memberRepository: MemberRepository,
        private val memberGroupRepository: MemberGroupRepository,
        private val mailchimpClient: MailchimpClient
) {

    companion object {
        val logger = LoggerFactory.getLogger(MailchimpController::class.java)
    }

    @EventListener
    fun handleMemberEvent(event: MemberEvent) {
        val classes = arrayOf(
                CreateMemberEvent::class,
                UpdateMemberEvent::class)
        val member = event.member
        if (event::class in classes && member.email != null) {
            syncMember(member)
        }

    }

    @EventListener
    fun handleMailchimpWebhookEvent(event: MailchimpWebhookEvent) {

        memberRepository.findByEmail(event.email)
                .map {
                    val value = when (event.type) {
                        MailchimpWebhookEventType.SUBSCRIBE -> true
                        MailchimpWebhookEventType.UNSUBSCRIBE -> false
                        else -> false
                    }
                    it.fields
                            .plus("newsletter" to value.toString())
                            .let { fields -> it.copy(fields = fields) }

                }
                .let {
                    memberRepository.saveAll(it)
                }
    }

    @GetMapping("/sync")
    fun sync() {
        syncGroups()
        memberRepository
                .findAll()
                .forEach {
                    syncMember(it)
                }
    }

    private fun syncGroups() {
        memberGroupRepository.findAll()
                .forEach {
                    mailchimpClient.postSegment(it.code)
                }
    }

    private fun syncMember(member: Member) {
        try {
            member.email?.also { email ->
                val new = constructMailchimpMember(member)
                val current = mailchimpClient.getMember(email)
                if (current != null) {
                    mailchimpClient.putMember(new)
                    mailchimpClient.putTags(email, new.tags, current.tags.minus(new.tags))
                } else {
                    mailchimpClient.postMember(new)
                }
            }
        }catch (ex:Exception){
            logger.info(ex.message)
        }
    }

    private fun constructMailchimpMember(member: Member): MailchimpMember {
        return MailchimpMember(
                firstName = member.infix
                        ?.let { member.firstName + " " + it }
                        ?: member.firstName,
                lastName = member.surName,
                email = member.email ?: "",
                status = member.fields
                        .getOrDefault("newsletter", "false")
                        .let {
                            if (it == "true")
                                MailchimpMemberStatus.SUBSCRIBED
                            else
                                MailchimpMemberStatus.UNSUBSCRIBED
                        },
                tags = member.groups
                        .map { it.code }
                        .toSet()

        )
    }
}
