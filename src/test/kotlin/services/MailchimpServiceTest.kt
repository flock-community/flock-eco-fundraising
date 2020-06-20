package community.flock.eco.fundraising.services

import com.nhaarman.mockitokotlin2.anyOrNull
import community.flock.eco.feature.mailchimp.clients.MailchimpClient
import community.flock.eco.feature.mailchimp.events.MailchimpWebhookEvent
import community.flock.eco.feature.mailchimp.events.MailchimpWebhookEventType
import community.flock.eco.feature.mailchimp.model.MailchimpInterest
import community.flock.eco.feature.mailchimp.model.MailchimpInterestCategory
import community.flock.eco.feature.mailchimp.model.MailchimpInterestCategoryType
import community.flock.eco.feature.member.model.Member
import community.flock.eco.feature.member.services.MemberService
import community.flock.eco.fundraising.Application
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.data.domain.PageRequest
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit4.SpringRunner
import java.time.LocalDateTime
import java.util.*
import kotlin.test.assertEquals

@RunWith(SpringRunner::class)
@SpringBootTest(
        classes = [Application::class],
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase
@TestPropertySource(properties = ["flock.fundraising.mailchimp.enabled=true"])
internal class MailchimpServiceTest() {

    companion object {
        private val mock = mock(MailchimpClient::class.java)
    }

    @TestConfiguration
    class TestConfig {

        @Bean
        @Primary
        fun mailchimpClient(): MailchimpClient {

            `when`(mock.getInterestsCategories(anyString())).thenReturn(listOf(MailchimpInterestCategory(
                    title = "Doneasy",
                    type = MailchimpInterestCategoryType.CHECKBOXES
            )))
            `when`(mock.getInterests(anyString(), anyString())).thenReturn(listOf(
                    MailchimpInterest(
                            id = "1",
                            name = "TRANSACTIONAL"
                    ),
                    MailchimpInterest(
                            id = "2",
                            name = "NEWSLETTER"
                    )
            ))

            return mock
        }
    }

    @Autowired
    lateinit var applicationEventPublisher: ApplicationEventPublisher

    @Autowired
    lateinit var memberService: MemberService

    @Test
    fun `should create member when member SUBSCRIBES in mailchimp`() {
        val event = MailchimpWebhookEvent(
                id = UUID.randomUUID().toString(),
                listId = UUID.randomUUID().toString(),
                firedAt = LocalDateTime.now(),
                interests = setOf(),
                type = MailchimpWebhookEventType.SUBSCRIBE,
                email = "new@email.nl"
        )
        applicationEventPublisher.publishEvent(event)

        val list = memberService.findAll(PageRequest.of(0, 100))

        assertEquals("<empty>", list.first().firstName)
        assertEquals("<empty>", list.first().surName)
        assertEquals("new@email.nl", list.first().email)

    }

    @Test
    fun `should create member when member SUBSCRIBES in mailchimp with name`() {
        val event = MailchimpWebhookEvent(
                id = UUID.randomUUID().toString(),
                listId = UUID.randomUUID().toString(),
                firedAt = LocalDateTime.now(),
                interests = setOf(),
                fields = mapOf(
                        "FNAME" to "FirstName",
                        "LNAME" to "LastName",
                        "LANGUAGE" to "NL"
                ),
                type = MailchimpWebhookEventType.SUBSCRIBE,
                email = "new@email.nl"
        )
        applicationEventPublisher.publishEvent(event)

        val list = memberService.findAll(PageRequest.of(0, 100))

        assertEquals("FirstName", list.first().firstName)
        assertEquals("LastName", list.first().surName)
        assertEquals("nl", list.first().language)
        assertEquals("new@email.nl", list.first().email)

    }

    @Test
    fun `should sync member with mailchimp when member create`() {

//        val putMemberSpy = spy(mock.putMember(anyString(), anyOrNull()))
//
//        val member = memberService.create(Member(
//                firstName = "firstName",
//                surName = "surName",
//                email = "new@member.nl",
//                language = "nl"
//        ))

//        assertEquals("FirstName", list.first().firstName)
//        assertEquals("LastName", list.first().surName)
//        assertEquals("nl", list.first().language)
//        assertEquals("new@email.nl", list.first().email)

    }
}
