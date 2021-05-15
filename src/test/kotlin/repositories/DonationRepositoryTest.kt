package community.flock.eco.fundraising.repositories

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import community.flock.eco.feature.member.model.Member
import community.flock.eco.feature.member.model.MemberGroup
import community.flock.eco.feature.member.repositories.MemberGroupRepository
import community.flock.eco.feature.member.repositories.MemberRepository
import community.flock.eco.feature.payment.model.PaymentFrequency
import community.flock.eco.feature.payment.model.PaymentMandate
import community.flock.eco.feature.payment.model.PaymentType
import community.flock.eco.feature.payment.repositories.PaymentMandateRepository
import community.flock.eco.fundraising.ApplicationConfiguration
import community.flock.eco.fundraising.model.Donation
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.data.domain.PageRequest
import org.springframework.test.context.ActiveProfiles
import java.util.*

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase
@ActiveProfiles(profiles = ["develop"])
@Import(ApplicationConfiguration::class)
internal class DonationRepositoryTest {

    @Autowired
    lateinit var donationRepository: DonationRepository

    @Autowired
    lateinit var paymentMandateRepository: PaymentMandateRepository

    @Autowired
    lateinit var memberRepository: MemberRepository

    @Autowired
    lateinit var memberGroupRepository: MemberGroupRepository

    @Test
    fun toJson() {
        val mapper = jacksonObjectMapper()

        val page = PageRequest.of(0, 100)
        val res = donationRepository.findAll(page)
        val string = mapper.writeValueAsString(res)
    }

    @Test
    fun testsCreate() {
        val email = UUID.randomUUID().toString()

        val group = MemberGroup(
            code = "TEST1",
            name = "Test1"
        ).let { memberGroupRepository.save(it) }

        val member = Member(
            firstName = "DonateFirstName",
            surName = "DonateSurName",
            email = email,
            groups = mutableSetOf(group)
        ).let { memberRepository.save(it) }

        val mandate = PaymentMandate(
            amount = 10.10,
            frequency = PaymentFrequency.MONTHLY,
            type = PaymentType.SEPA

        ).let { paymentMandateRepository.save(it) }

        val donation = Donation(
            member = member,
            mandate = mandate
        ).let { donationRepository.save(it) }

        val res = donationRepository.findByMemberId(donation.member!!.id).first()

        assertEquals("TEST1", res.member!!.groups.toList()[0].code)
    }
}
