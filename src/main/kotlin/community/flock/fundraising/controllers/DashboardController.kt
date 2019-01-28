package community.flock.fundraising.controllers

import community.flock.eco.feature.member.model.Member
import community.flock.eco.feature.member.model.MemberStatus
import community.flock.eco.feature.member.repositories.MemberRepository
import community.flock.eco.feature.member.specifications.MemberSpecification
import community.flock.eco.feature.payment.model.PaymentFrequency
import community.flock.eco.feature.payment.model.PaymentMandate
import community.flock.eco.feature.payment.model.PaymentTransactionStatus
import community.flock.eco.feature.payment.model.PaymentType.*
import community.flock.eco.feature.payment.repositories.PaymentMandateRepository
import community.flock.fundraising.model.Donation
import community.flock.fundraising.repositories.DonationRepository
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate

@RestController
@RequestMapping("/api/dashboard")
class DashboardController(
        val donationRepository: DonationRepository,
        val paymentMandateRepository: PaymentMandateRepository,
        val memberRepository: MemberRepository) {

    data class DashboardModel(
            val totalCollectionValue: Int,
            val totalDonationsOnce: Map<String, Double>,
            val totalMembers: Int,
            val totalMandates: Int,
            val newMembers: List<Member>,
            val newDonations: List<Donation>
    )

    @GetMapping()
    fun index(): DashboardModel {
        val mandates = paymentMandateRepository.findAll().toList()
        return DashboardModel(
                totalCollectionValue = countTotalMandates(mandates),
                totalDonationsOnce = countTotalDonations(mandates),
                totalMandates = countMandate(mandates),
                totalMembers = countMembers(),
                newMembers = newMembers(),
                newDonations = newDonations()
        )
    }

    fun newMembers(): List<Member> {
        val sort = Sort(Sort.Direction.DESC, "id")
        val page = PageRequest.of(0, 5, sort)
        val specification = MemberSpecification(statuses = setOf(MemberStatus.NEW))
        return memberRepository.findAll(specification, page)
                .toList()
    }

    fun newDonations(): List<Donation> {
        val sort = Sort(Sort.Direction.DESC, "id")
        val page = PageRequest.of(0, 5, sort)
        return donationRepository.findAll(page)
                .toList()
    }

    fun countMembers(): Int {
        return memberRepository.findAll()
                .filter { it.status != MemberStatus.DELETED }
                .filter { it.status != MemberStatus.MERGED }
                .map { 1 }
                .fold(0) { cur, acc -> (acc + cur) }
    }

    fun countMandate(mandates: List<PaymentMandate>): Int {
        return mandates
                .filter { it.endDate == null }
                .map { 1 }
                .fold(0) { cur, acc -> (acc + cur) }
    }

    fun countTotalMandates(mandates: List<PaymentMandate>): Int {
        return mandates
                .filter { it.type == SEPA }
                .filter { it.endDate == null }
                .map {
                    when (it.frequency) {
                        PaymentFrequency.ONCE -> 0.0
                        PaymentFrequency.MONTHLY -> it.amount * 12
                        PaymentFrequency.QUARTERLY -> it.amount * 4
                        PaymentFrequency.HALF_YEARLY -> it.amount * 2
                        PaymentFrequency.YEARLY -> it.amount
                    }
                }
                .fold(0.0) { cur, acc -> (acc + cur) }
                .let { it.toInt() }
    }

    fun countTotalDonations(mandates: List<PaymentMandate>): Map<String, Double> {
        val now = LocalDate.now()
        val types = arrayOf(IDEAL, CREDIT_CARD)
        return mandates
                .filter { types.contains(it.type) }
                .flatMap { it.transactions }
                .filter { it.created.isAfter(now.minusYears(1)) }
                .filter { it.status == PaymentTransactionStatus.SUCCESS }
                .groupBy { it.created.month.name }
                .mapValues { (month, transactions) -> transactions.sumByDouble { it -> it.amount } }

    }
}