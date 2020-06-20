package community.flock.eco.fundraising.services

import community.flock.eco.feature.mailchimp.clients.MailchimpClient
import community.flock.eco.feature.mailchimp.events.MailchimpWebhookEvent
import community.flock.eco.feature.mailchimp.events.MailchimpWebhookEventType
import community.flock.eco.feature.mailchimp.events.MailchimpWebhookEventType.*
import community.flock.eco.feature.mailchimp.model.*
import community.flock.eco.feature.member.events.CreateMemberEvent
import community.flock.eco.feature.member.events.MemberEvent
import community.flock.eco.feature.member.events.UpdateMemberEvent
import community.flock.eco.feature.member.model.Member
import community.flock.eco.feature.member.model.MemberStatus
import community.flock.eco.feature.member.repositories.MemberGroupRepository
import community.flock.eco.feature.member.repositories.MemberRepository
import community.flock.eco.feature.member.services.MemberService
import community.flock.eco.fundraising.services.MemberFieldService.MemberFields.*
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Service
import javax.annotation.PostConstruct

@Service
@ConditionalOnProperty("flock.fundraising.mailchimp.enabled", havingValue = "true", matchIfMissing = true)
class MailchimpService(
        private val memberService: MemberService,
        private val memberRepository: MemberRepository,
        private val memberGroupRepository: MemberGroupRepository,
        private val mailchimpClient: MailchimpClient
) {

    private val interestCategory: String = "Doneasy"

    enum class Interest {
        TRANSACTIONAL,
        NEWSLETTER
    }

    fun Member.isActive(): Boolean {
        val active = listOf(MemberStatus.DISABLED, MemberStatus.ACTIVE, MemberStatus.NEW)
        return active.contains(this.status)
    }

    @Value("\${flock.eco.feature.mailchimp.listId:}")
    private lateinit var listId: String
    private lateinit var category: MailchimpInterestCategory
    private lateinit var interests: Map<Interest, MailchimpInterest>

    companion object {
        val logger = LoggerFactory.getLogger(MailchimpService::class.java)
    }

    @PostConstruct
    fun init() {
        category = findInterestsCategory()
        interests = mailchimpClient.getInterests(listId, category.id)
                .map { Interest.valueOf(it.name) to it }
                .toMap()
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
        if (listOf(SUBSCRIBE, UNSUBSCRIBE, PROFILE).contains(event.type)) {
            val activeList = memberService.findAllByEmail(event.email)
                    .filter { it.isActive() }

            if (activeList.isNotEmpty()) {
                activeList
                        .map {
                            it.copy(
                                    fields = it.fields
                                            .plus(NEWSLETTER.key to Interest.NEWSLETTER.inList(event.interests))
                                            .plus(TRANSACTIONAL_MAIL.key to Interest.TRANSACTIONAL.inList(event.interests))
                                            .plus(MAILCHIMP_STATUS.key to (event.type.getStatus()?.name
                                                    ?: it.fields.getValue(MAILCHIMP_STATUS.key))))
                        }
                        .map {
                            if (activeList.size == 1) {
                                it.copy(
                                        firstName = event.fields["FNAME"] ?: "<empty>",
                                        surName = event.fields["LNAME"] ?: "<empty>",
                                        language =  event.fields["LANGUAGE"]?.toLowerCase()
                                )
                            } else {
                                it
                            }
                        }
                        .let {
                            memberRepository.saveAll(it)
                        }
            } else {
                Member(
                        firstName = event.fields["FNAME"] ?: "<empty>",
                        surName = event.fields["LNAME"] ?: "<empty>",
                        email = event.email,
                        status = MemberStatus.NEW,
                        fields = mapOf(
                                NEWSLETTER.key to Interest.NEWSLETTER.inList(event.interests),
                                TRANSACTIONAL_MAIL.key to Interest.TRANSACTIONAL.inList(event.interests),
                                MAILCHIMP_STATUS.key to MailchimpMemberStatus.SUBSCRIBED.name
                        ),
                        language =  event.fields["LANGUAGE"]?.toLowerCase()
                ).run { memberRepository.save(this) }
            }
        }
    }

    fun syncGroups() {
        memberGroupRepository.findAll()
                .forEach {
                    mailchimpClient.postSegment(listId, it.code)
                }
    }

    fun syncMembers() {
        memberRepository
                .findAll()
                .forEach {
                    syncMember(it)
                }
    }

    private fun findInterestsCategory() = mailchimpClient
            .getInterestsCategories(listId)
            .find { it.title == interestCategory }
            ?: MailchimpInterestCategory(id = "0", title = interestCategory, type = MailchimpInterestCategoryType.CHECKBOXES)
                    .let { mailchimpClient.postInterestsCategories(listId, it) }
                    ?.also { category ->
                        Interest.values().forEach {
                            mailchimpClient.postInterests(listId, category.id, MailchimpInterest(name = it.name))
                        }
                    }
            ?: throw RuntimeException("Interests category not found")


    private fun syncMember(member: Member) {
        try {
            member.email?.also { email ->
                val new = constructMailchimpMember(member)
                val current = mailchimpClient.getMember(listId, email)
                if (current != null) {
                    mailchimpClient.putMember(listId, new)
                    mailchimpClient.putTags(listId, email, new.tags, current.tags.minus(new.tags))
                } else {
                    mailchimpClient.postMember(listId, new)
                }
            }
        } catch (ex: Exception) {
            logger.error(ex.message, ex)
        }
    }

    private fun constructMailchimpMember(member: Member): MailchimpMember {
        val transactional = member.getFieldValue(TRANSACTIONAL_MAIL)
                ?.toBoolean()
                ?: true
        val newsletter = member.getFieldValue(NEWSLETTER)
                ?.toBoolean()
                ?: false
        val status = member.getFieldValue(MAILCHIMP_STATUS)
                ?.let { MailchimpMemberStatus.valueOf(it) }
                ?: MailchimpMemberStatus.SUBSCRIBED
        return MailchimpMember(
                email = member.email ?: "",
                status = status,
                tags = member.groups
                        .map { it.code }
                        .toSet(),
                fields = mapOf(
                        "FNAME" to (member.infix?.let { member.firstName + " " + it } ?: member.firstName),
                        "LNAME" to member.surName,
                        "LANGUAGE" to member.language?.toUpperCase()
                ),
                language = member.language,
                interests = mapOf(
                        interests.getValue(Interest.TRANSACTIONAL).id to transactional,
                        interests.getValue(Interest.NEWSLETTER).id to newsletter)
        )
    }

    private fun MailchimpWebhookEventType.getStatus(): MailchimpMemberStatus? = when (this) {
        SUBSCRIBE -> MailchimpMemberStatus.SUBSCRIBED
        UNSUBSCRIBE -> MailchimpMemberStatus.UNSUBSCRIBED
        else -> null
    }

    private infix fun Interest.inList(interests: Set<String>) = interests
            .contains(this.name)
            .toString()

}
