package community.flock.eco.fundraising.controllers

import community.flock.eco.core.utils.toNullable
import community.flock.eco.core.utils.toResponse
import community.flock.eco.feature.mailchimp.model.MailchimpMemberStatus
import community.flock.eco.feature.member.events.MergeMemberEvent
import community.flock.eco.feature.member.model.Member
import community.flock.eco.feature.member.model.MemberGroup
import community.flock.eco.feature.member.repositories.MemberGroupRepository
import community.flock.eco.feature.member.repositories.MemberRepository
import community.flock.eco.feature.payment.model.PaymentBankAccount
import community.flock.eco.feature.payment.model.PaymentFrequency
import community.flock.eco.feature.payment.model.PaymentMandate
import community.flock.eco.feature.payment.model.PaymentType
import community.flock.eco.feature.payment.repositories.PaymentMandateRepository
import community.flock.eco.feature.payment.repositories.PaymentTransactionRepository
import community.flock.eco.feature.payment.services.PaymentBuckarooService
import community.flock.eco.feature.payment.services.PaymentSepaService
import community.flock.eco.fundraising.model.Donation
import community.flock.eco.fundraising.repositories.DonationRepository
import community.flock.eco.fundraising.services.MemberFieldService
import community.flock.eco.fundraising.services.MemberFieldService.MemberFields.*
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.event.EventListener
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import org.springframework.web.util.UriComponentsBuilder
import java.net.URL
import java.time.LocalDate
import javax.servlet.http.HttpServletRequest
import javax.transaction.Transactional

private val ibanRegex = "^([A-Z]{2}[ \\-]?[0-9]{2})(?=(?:[ \\-]?[A-Z0-9]){9,30}\$)((?:[ \\-]?[A-Z0-9]{3,5}){2,7})([ \\-]?[A-Z0-9]{1,3})?\$".toRegex()

@RestController
@RequestMapping("/api/donations")
class DonationsController(
    private val request: HttpServletRequest,
    private val buckarooService: PaymentBuckarooService,
    private val sepaService: PaymentSepaService,
    private val memberRepository: MemberRepository,
    private val memberGroupRepository: MemberGroupRepository,
    private val memberFieldService: MemberFieldService,
    private val paymentMandateRepository: PaymentMandateRepository,
    private val paymentTransactionRepository: PaymentTransactionRepository,
    private val donationRepository: DonationRepository
) {

    @Value("\${flock.fundraising.donations.successPath:}")
    lateinit var successPath: String

    @Value("\${flock.fundraising.donations.failurePath:}")
    lateinit var failurePath: String

    data class Donate(
        val payment: Payment,
        val member: Member? = null,
        val newsletter: Boolean,
        val language: String?,
        val agreedOnTerms: Boolean,
        val group: String? = null,
        val destination: String? = null
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
        val member: Member?,
        val destination: String? = null
    )

    data class DonationStopForm(
        val reason: String?
    )

    @EventListener
    fun handleMergeMemberEvent(event: MergeMemberEvent) {
        event.mergedMembers
            .flatMap { donationRepository.findByMemberId(it.id) }
            .map { it.copy(member = event.member) }
            .toList()
            .let { donationRepository.saveAll(it) }
    }

    @GetMapping
    @PreAuthorize("hasAuthority('DonationsAuthority.READ')")
    fun findAll(
        @RequestParam("s") search: String = "",
        page: Pageable
    ): ResponseEntity<List<Donation>> {
        val res = if (search.isEmpty()) {
            val sort = Sort.by(Sort.Direction.DESC, "id")
            val pageSort = PageRequest.of(page.pageNumber, page.pageSize, sort)
            donationRepository.findAll(pageSort)
        } else {
            donationRepository.findBySearch(search, page)
        }
        val headers = HttpHeaders()
        headers.set("x-page", page.pageNumber.toString())
        headers.set("x-total", res.totalElements.toString())
        return ResponseEntity(res.content.toList(), headers, HttpStatus.OK)
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('DonationsAuthority.READ')")
    fun findById(@PathVariable id: Long): ResponseEntity<Donation> {
        return donationRepository.findById(id).toResponse()
    }

    @PostMapping("/donate")
    fun donate(@RequestBody donate: Donate): ResponseEntity<String> {

        if (donate.member != null && donate.member.firstName.isEmpty()) {
            throw error("First name required")
        }
        if (donate.member != null && donate.member.surName.isEmpty()) {
            throw error("Sur name required")
        }
        if (donate.newsletter && donate.member?.email.isNullOrEmpty()) {
            throw error("Email required when subscribe for newsletter")
        }
        if (donate.payment.paymentType == PaymentType.SEPA) {
            if (donate.payment.bankAccount?.iban?.matches(ibanRegex) == false) {
                throw error("Not a valid IBAN number")
            }
        }

        val groupDonation = donate.group
            ?.let {
                memberGroupRepository
                    .findByCode(it.toUpperCase())
                    .toNullable()
                    ?: MemberGroup(
                        name = it,
                        code = it.toUpperCase()
                    )
                        .run { memberGroupRepository.save(this) }
            }

        val member = donate.member
            ?.copy(
                groups = groupDonation?.run { mutableSetOf(this) } ?: mutableSetOf(),
                fields = mutableMapOf(
                    NEWSLETTER.key to donate.newsletter.toString().toLowerCase(),
                    AGREED_ON_TERMS.key to donate.agreedOnTerms.toString().toLowerCase(),
                    TRANSACTIONAL_MAIL.key to "true",
                    MAILCHIMP_STATUS.key to MailchimpMemberStatus.SUBSCRIBED.name
                ),
                language = donate.language?.toLowerCase()
            )
            ?.run { memberRepository.save(this) }

        val successUrl = request.extractRequestor() + successPath
        val failureUrl = request.extractRequestor() + failurePath

        fun PaymentMandate.createDonation() {
            Donation(
                member = member,
                mandate = this,
                destination = donate.destination
            ).let {
                donationRepository.save(it)
            }
        }

        return when (donate.payment.paymentType) {

            PaymentType.IDEAL -> {
                PaymentBuckarooService.PaymentMethod.Ideal(
                    amount = donate.payment.amount,
                    description = "Donation",
                    issuer = donate.payment.issuer!!,
                    successUrl = successUrl,
                    failureUrl = failureUrl
                )
                    .let { buckarooService.create(it) }
                    .apply { mandate.createDonation() }
                    .run { ResponseEntity.ok(redirectUrl) }
            }

            PaymentType.CREDIT_CARD -> {
                PaymentBuckarooService.PaymentMethod.CreditCard(
                    amount = donate.payment.amount,
                    description = "Donation",
                    issuer = donate.payment.issuer!!,
                    successUrl = successUrl,
                    failureUrl = failureUrl
                )
                    .let { buckarooService.create(it) }
                    .apply { mandate.createDonation() }
                    .run { ResponseEntity.ok(redirectUrl) }
            }
            PaymentType.SEPA -> {
                PaymentSepaService.PaymentSepa(
                    amount = donate.payment.amount,
                    bankAccount = donate.payment.bankAccount!!,
                    frequency = donate.payment.frequency!!
                )
                    .let { sepaService.create(it) }
                    .apply { mandate.createDonation() }
                    .run { ResponseEntity.ok(successUrl) }
            }

            PaymentType.CACHE -> throw error("cannot make cache donations")
        }
    }

    @GetMapping("/donate")
    @PreAuthorize("hasAuthority('DonationsAuthority.READ')")
    fun get(): ResponseEntity<Unit> {
        memberFieldService.init()
        return ResponseEntity.noContent().build()
    }

    @PostMapping()
    @PreAuthorize("hasAuthority('DonationsAuthority.WRITE')")
    fun create(@RequestBody form: DonationForm): ResponseEntity<Donation> {
        memberFieldService.init()
        val donation = Donation(
            member = form.member
                ?.let { memberRepository.save(it) },
            mandate = paymentMandateRepository.save(form.mandate),
            destination = form.destination
        ).let {
            donationRepository.save(it)
        }

        return ResponseEntity.ok(donation)
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('DonationsAuthority.WRITE')")
    fun update(@PathVariable id: Long, @RequestBody form: DonationForm): ResponseEntity<Donation> {
        memberFieldService.init()
        return donationRepository.findById(id)
            .map { donation ->
                donation.copy(
                    member = form.member
                        ?.let { memberRepository.save(it) },
                    mandate = paymentMandateRepository.save(form.mandate),
                    destination = form.destination
                ).let {
                    donationRepository.save(it)
                }
            }
            .toResponse()
    }

    @PostMapping("/{id}/stop")
    @PreAuthorize("hasAuthority('DonationsAuthority.WRITE')")
    fun stop(@PathVariable id: Long, @RequestBody form: DonationStopForm) {

        donationRepository.findById(id).ifPresent { donation ->
            form.reason?.let { reason ->
                donation.member?.let { member ->
                    member
                        .copy(fields = (member.fields + (TERMINATION_REASON.key to reason)).toMutableMap())
                        .let { memberRepository.save(it) }
                }
            }

            donation.mandate
                .copy(endDate = LocalDate.now())
                .let { paymentMandateRepository.save(it) }
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('DonationsAuthority.WRITE')")
    @Transactional
    fun delete(@PathVariable id: Long) {
        donationRepository.findById(id)
            .toNullable()
            ?.let {
                val mandateId = it.mandate.id
                val transactionIds = it.mandate.transactions.map { it.id }
                donationRepository.deleteById(it.id)
                transactionIds.forEach {
                    paymentTransactionRepository.deleteById(it)
                }
                paymentMandateRepository.deleteById(mandateId)
            }
    }

    private fun HttpServletRequest.extractRequestor(): String = when {
        (this.getHeader("origin") != null) -> this.getHeader("origin")
        (this.getHeader("x-forwarded-host") != null) -> {
            val proto = this.getHeader("x-forwarded-proto") ?: "https"
            val host = this.getHeader("x-forwarded-host")
            val port = this.getHeader("x-forwarded-port").toInt()
            UriComponentsBuilder.newInstance()
                .scheme(proto)
                .host(host)
                .port(port)
                .build()
                .toUriString()
        }
        else -> {
            val url = URL(this.requestURL.toString())
            UriComponentsBuilder.newInstance()
                .scheme(url.protocol)
                .host(url.host)
                .port(url.port)
                .build()
                .toUriString()
        }
    }
}
