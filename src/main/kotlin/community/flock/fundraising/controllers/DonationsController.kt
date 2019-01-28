package community.flock.fundraising.controllers

import community.flock.eco.feature.member.controllers.MergeMemberEvent
import community.flock.eco.feature.member.model.Member
import community.flock.eco.feature.member.model.MemberGroup
import community.flock.eco.feature.member.repositories.MemberGroupRepository
import community.flock.eco.feature.member.repositories.MemberRepository
import community.flock.eco.feature.member.services.MemberService
import community.flock.eco.feature.payment.model.*
import community.flock.eco.feature.payment.repositories.PaymentMandateRepository
import community.flock.eco.feature.payment.repositories.PaymentTransactionRepository
import community.flock.eco.feature.payment.services.PaymentBuckarooService
import community.flock.eco.feature.payment.services.PaymentSepaService
import community.flock.fundraising.model.Donation
import community.flock.fundraising.repositories.DonationRepository
import community.flock.fundraising.service.MemberFieldService
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.event.EventListener
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import java.time.LocalDate

@RestController
@RequestMapping("/api/donations")
class DonationsController(
        private val buckarooService: PaymentBuckarooService,
        private val sepaService: PaymentSepaService,
        private val memberService: MemberService,
        private val memberRepository: MemberRepository,
        private val memberGroupRepository: MemberGroupRepository,
        private val memberFieldService: MemberFieldService,
        private val paymentMandateRepository: PaymentMandateRepository,
        private val donationRepository: DonationRepository,
        private val paymentTransactionRepository: PaymentTransactionRepository) {

    @Value("\${flock.fundraising.donations.successUrl:@null}")
    lateinit var successUrl: String

    @Value("\${flock.fundraising.donations.failureUrl:@null}")
    lateinit var failureUrl: String

    companion object {
        const val FIELD_NEWSLETTER = "newsletter"
        const val FIELD_AGREED_ON_TERMS = "agreed_on_terms"
        const val TERMINATION_REASON = "termination_reason"
    }

    data class Donate(
            val payment: Payment,
            val member: Member? = null,
            val newsletter: Boolean,
            val agreedOnTerms: Boolean,
            val group: String? = null
    )

    data class Payment(
            val amount: Double,
            val paymentType: PaymentType,
            val issuer: String? = null,
            val frequency: PaymentFrequency? = null,
            val bankAccount: PaymentBankAccount? = null
    )

    data class DonationForm(
            val mandate: PaymentMandate,
            val member: Member
    )

    data class DonationStopForm(
            val reason: String?
    )

    @EventListener
    fun handleMergeMemberEvent(event: MergeMemberEvent) {
        event.mergeMembers
                .flatMap { donationRepository.findByMemberId(it.id) }
                .map { it.copy(member = event.member) }
                .toList()
                .let { donationRepository.saveAll(it) }
    }

    @GetMapping
    @PreAuthorize("hasAuthority('MemberAuthority.READ')")
    fun findAll(
            @RequestParam("s") search: String = "",
            page: Pageable): ResponseEntity<List<Donation>> {
        val res = donationRepository.findBySearch(search, page)
        val headers = HttpHeaders()
        headers.set("x-page", page.pageNumber.toString())
        headers.set("x-total", res.totalElements.toString())
        return ResponseEntity(res.content.toList(), headers, HttpStatus.OK)
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('DonationsAuthority.READ')")
    fun findById(@PathVariable id: Long): ResponseEntity<Donation> {
        return donationRepository.findById(id)
                .map {
                    ResponseEntity(it, HttpStatus.OK)
                }.orElseGet {
                    ResponseEntity(HttpStatus.NOT_FOUND)
                }
    }

    data class ResponseMandateTransaction(val mandate: PaymentMandate, val transaction: PaymentTransaction)

    @GetMapping("/transactions/{year}/{month}")
    @PreAuthorize("hasAuthority('DonationsAuthority.READ')")
    fun findByMonthAndYear(
            @PathVariable("month") month: Int,
            @PathVariable("year") year: Int,
            page: Pageable): ResponseEntity<List<ResponseMandateTransaction>> {

        val startDate = LocalDate.of(year, month, 1)
        val endDate = startDate.withDayOfMonth(startDate.lengthOfMonth())
        val type = PaymentType.SEPA
        val res = paymentTransactionRepository.findBetweenDate(startDate, endDate, type, page)
        val entity = res.content
                .map { ResponseMandateTransaction(it.mandate, it) }
        val headers = HttpHeaders()
        headers.set("x-page", page.pageNumber.toString())
        headers.set("x-total", res.totalElements.toString())
        return ResponseEntity(entity, headers, HttpStatus.OK)
    }

    @PostMapping("/transactions/{year}/{month}/success")
    @PreAuthorize("hasAuthority('DonationsAuthority.WRITE')")
    fun markTransactionsSuccess(
            @PathVariable("month") month: Int,
            @PathVariable("year") year: Int): ResponseEntity<List<Donation>> {

        val startDate = LocalDate.of(year, month, 1)
        val endDate = startDate.withDayOfMonth(startDate.lengthOfMonth())

        return paymentTransactionRepository.findBetweenDate(startDate, endDate)
                .filter { it.mandate.type == PaymentType.SEPA }
                .filter { it.status == PaymentTransactionStatus.PENDING }
                .map {
                    it.copy(
                            status = PaymentTransactionStatus.SUCCESS
                    )
                }
                .let { paymentTransactionRepository.saveAll(it) }
                .let { ResponseEntity(HttpStatus.OK) }
    }

    @PostMapping("/transactions/{id}/status/{status}")
    @PreAuthorize("hasAuthority('DonationsAuthority.WRITE')")
    fun markTransactionStatus(
            @PathVariable("id") id: Long,
            @PathVariable("status") status: String): ResponseEntity<Any> {

        return paymentTransactionRepository.findById(id)
                .map {
                    it.copy(
                            status = PaymentTransactionStatus
                                    .valueOf(status.toUpperCase())
                    )
                }
                .map { paymentTransactionRepository.save(it) }
                .map { ResponseEntity<Any>(HttpStatus.OK) }
                .orElseGet { ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR) }

    }

    @PostMapping("/donate")
    fun donate(@RequestBody donate: Donate): ResponseEntity<String> {

        val groupDonation = donate.group?.let {
            memberGroupRepository
                    .findByCode(it.toUpperCase())
                    .orElseGet {
                        MemberGroup(
                                name = it,
                                code = it.toUpperCase()
                        ).let {
                            memberGroupRepository.save(it)
                        }
                    }
        }

        val member = donate.member?.let {
            it.copy(
                    groups = groupDonation?.let { setOf(it) } ?: setOf(),
                    fields = mapOf(
                            FIELD_NEWSLETTER to donate.newsletter.toString().toLowerCase(),
                            FIELD_AGREED_ON_TERMS to donate.agreedOnTerms.toString().toLowerCase()
                    )
            ).let {
                memberService.create(it)
            }
        }

        return when (donate.payment.paymentType) {

            PaymentType.IDEAL -> {
                PaymentBuckarooService.PaymentMethod.Ideal(
                        amount = donate.payment.amount,
                        description = "Donation",
                        issuer = donate.payment.issuer!!
                ).let {
                    buckarooService.create(it)
                            .let {
                                Donation(
                                        member = member,
                                        mandate = it.mandate
                                ).let {
                                    donationRepository.save(it)
                                }
                                ResponseEntity.ok(it.redirectUrl)
                            }
                }
            }
            PaymentType.CREDIT_CARD -> {
                PaymentBuckarooService.PaymentMethod.CreditCard(
                        amount = donate.payment.amount,
                        description = "Donation",
                        issuer = donate.payment.issuer!!
                ).let {
                    buckarooService.create(it)
                            .let {
                                Donation(
                                        member = member,
                                        mandate = it.mandate
                                ).let {
                                    donationRepository.save(it)
                                }
                                ResponseEntity.ok(it.redirectUrl)
                            }
                }
            }
            PaymentType.SEPA -> {
                PaymentSepaService.PaymentSepa(
                        amount = donate.payment.amount,
                        bankAccount = donate.payment.bankAccount!!,
                        frequency = donate.payment.frequency!!
                ).let {
                    sepaService.create(it)
                            .let {
                                Donation(
                                        member = member,
                                        mandate = it.mandate
                                ).let {
                                    donationRepository.save(it)
                                }

                                ResponseEntity.ok(successUrl)
                            }
                }
            }
        }
    }

    @GetMapping("/donate")
    fun get(): ResponseEntity<Unit> {
        memberFieldService.init()
        return ResponseEntity.noContent().build()
    }

    @PostMapping()
    fun create(@RequestBody form: DonationForm): ResponseEntity<Donation> {
        memberFieldService.init()
        val donation = Donation(
                member = memberService.create(form.member),
                mandate = paymentMandateRepository.save(form.mandate)
        ).let {
            donationRepository.save(it)
        }

        return ResponseEntity.ok(donation)
    }

    @PostMapping("/{id}/stop")
    fun stop(@PathVariable id: Long, @RequestBody form: DonationStopForm) {

        donationRepository.findById(id).ifPresent { donation ->

            form.reason?.let { reason ->
                donation.member?.let { member ->
                    member
                            .copy(fields = member.fields + (TERMINATION_REASON to reason))
                            .let { memberService.update(it.id, it) }
                }
            }

            donation.mandate
                    .copy(endDate = LocalDate.now())
                    .let { paymentMandateRepository.save(it) }
        }
    }

}

