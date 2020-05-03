package community.flock.eco.fundraising


import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JSR310Module
import community.flock.eco.feature.member.model.Member
import community.flock.eco.feature.member.repositories.MemberRepository
import community.flock.eco.feature.member.services.MemberService
import community.flock.eco.feature.payment.model.PaymentBankAccount
import community.flock.eco.feature.payment.model.PaymentFrequency
import community.flock.eco.feature.payment.model.PaymentTransactionStatus
import community.flock.eco.feature.payment.model.PaymentType
import community.flock.eco.feature.payment.repositories.PaymentMandateRepository
import community.flock.eco.feature.payment.repositories.PaymentTransactionRepository
import community.flock.eco.fundraising.controllers.DonationsController
import community.flock.eco.fundraising.repositories.DonationRepository
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.util.*


@RunWith(SpringRunner::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@AutoConfigureTestDatabase
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@ActiveProfiles(profiles = ["local"])
class DonateIT {

    val mapper = ObjectMapper().registerModule(JSR310Module());

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var memberService: MemberService

    @Autowired
    lateinit var memberRepository: MemberRepository

    @Autowired
    lateinit var donationRepository: DonationRepository

    @Autowired
    lateinit var paymentTransactionRepository: PaymentTransactionRepository

    @Autowired
    lateinit var paymentMandateRepository: PaymentMandateRepository

    @Test
    fun donationWithMemberIdeal() {

        val email = UUID.randomUUID().toString();

        val member = Member(
                firstName = "DonateFirstName",
                surName = "DonateSurName",
                email = email
        )

        val payment = DonationsController.Payment(
                amount = 10.00,
                issuer = "INGBNL2A",
                paymentType = PaymentType.IDEAL
        )

        val donate = DonationsController.Donate(
                member = member,
                payment = payment,
                agreedOnTerms = true,
                newsletter = true,
                language = "nl"
        )

        mockMvc.perform(post("/api/donations/donate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsBytes(donate)))
                .andDo(print())
                .andExpect(status().isOk())

        val memberRes = memberService.findAllByEmail(email).first()
        val donation = donationRepository.findByMemberId(memberRes.id).first()
        val mandate = paymentMandateRepository.findById(donation.mandate.id).get()
        val transaction = paymentTransactionRepository.findByMandate(mandate).first()

        assertEquals(email, memberRes?.email)
        assertEquals(10.0, donation.mandate.amount, 0.0)
        assertEquals(PaymentType.IDEAL, donation.mandate.type)
        assertEquals(PaymentFrequency.ONCE, donation.mandate.frequency)
        assertNull(transaction.confirmed)
        assertEquals(PaymentTransactionStatus.PENDING, transaction.status)

        // Confirm Transcation
        mockMvc.perform(post("/api/payment/buckaroo/success")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"Transaction\": {\"Key\": \"${transaction.reference}\"}}"))
                .andDo(print())
                .andExpect(status().is2xxSuccessful)


        val transactionConfirm = paymentTransactionRepository.findByMandate(mandate).first()

        assertNotNull(transactionConfirm.confirmed)
        assertEquals(PaymentTransactionStatus.SUCCESS, transactionConfirm.status)

    }

    @Test
    fun donationWithMemberSepa() {

        val email = UUID.randomUUID().toString();

        val member = Member(
                firstName = "DonateFirstName",
                surName = "DonateSurName",
                email = email
        )

        val payment = DonationsController.Payment(
                amount = 10.00,
                paymentType = PaymentType.SEPA,
                frequency = PaymentFrequency.MONTHLY,
                bankAccount = PaymentBankAccount(
                        name = "W.F. Veelenturf",
                        iban = "NL00ABCD0000123456",
                        bic = "BIC1234",
                        country = "NL"
                )
        )

        val donate = DonationsController.Donate(
                member = member,
                payment = payment,
                agreedOnTerms = true,
                newsletter = true,
                language = "nl"
        )

        mockMvc.perform(post("/api/donations/donate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsBytes(donate)))
                .andDo(print())
                .andExpect(status().isOk())

        val memberRes = memberService.findAllByEmail(email).first()
        val donationRes = donationRepository.findByMemberId(memberRes.id).first()
        val transactionRes = paymentTransactionRepository.findByMandate(donationRes.mandate).toList()

        assertEquals(email, memberRes.email)
        assertEquals(10.0, donationRes.mandate.amount, 0.0)
        assertEquals(PaymentType.SEPA, donationRes.mandate.type)
        assertEquals(PaymentFrequency.MONTHLY, donationRes.mandate.frequency)
        assertEquals(0, transactionRes.size)

    }


    @Test
    fun donationAnoniemCreditCard() {

        val initCount = memberRepository.findAll().toList().size

        val payment = DonationsController.Payment(
                paymentType = PaymentType.CREDIT_CARD,
                amount = 10.00,
                issuer = "visa"
        )

        val donate = DonationsController.Donate(
                payment = payment,
                agreedOnTerms = true,
                newsletter = true,
                language = "nl"
        )

        mockMvc.perform(post("/api/donations/donate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsBytes(donate)))
                .andDo(print())
                .andExpect(status().is2xxSuccessful)

        val memberRes = memberRepository.findAll().toList()
        assertEquals(initCount, memberRes.size)


    }

}
