package community.flock.eco.fundraising.services

import community.flock.eco.feature.member.model.Member
import community.flock.eco.feature.member.model.MemberStatus
import community.flock.eco.feature.member.repositories.MemberRepository
import community.flock.eco.feature.member.specifications.MemberSpecification
import community.flock.eco.feature.payment.model.*
import community.flock.eco.fundraising.model.Donation
import community.flock.eco.fundraising.repositories.DonationRepository
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import java.time.LocalDate
import javax.persistence.EntityManager

@Service
class DashboardService(
    val entityManager: EntityManager,
    val donationRepository: DonationRepository,
    val memberRepository: MemberRepository
) {

    val months = listOf(
        "JANUARY",
        "FEBRUARY",
        "MARCH",
        "APRIL",
        "MAY",
        "JUNE",
        "JULY",
        "AUGUST",
        "SEPTEMBER",
        "OCTOBER",
        "NOVEMBER",
        "DECEMBER"
    )

    fun newMembers(): List<Member> {
        val sort = Sort.by(Sort.Direction.DESC, "id")
        val page = PageRequest.of(0, 5, sort)
        val specification = MemberSpecification(statuses = setOf(MemberStatus.NEW))
        return memberRepository.findAll(specification, page)
            .toList()
    }

    fun newDonationsOnce(): List<Donation> {
        val sort = Sort.by(Sort.Direction.DESC, "id")
        val page = PageRequest.of(0, 15, sort)
        return donationRepository.findAll(page).content
            .filter { donation -> donation.mandate.isOnce() }
            .filter { donation -> donation.mandate.isSuccess() }
            .take(5)
    }

    fun newDonationsRecurring(): List<Donation> {
        val sort = Sort.by(Sort.Direction.DESC, "id")
        val page = PageRequest.of(0, 15, sort)
        return donationRepository.findAll(page).content
            .filter { donation -> donation.mandate.isRecurring() }
            .take(5)
    }

    fun countMembers(): Int {
        val query = "SELECT COUNT(m) FROM Member m WHERE m.status NOT IN :status"
        return entityManager
            .createQuery(query)
            .setParameter("status", listOf(MemberStatus.DELETED, MemberStatus.MERGED))
            .singleResult
            .toString()
            .toInt()
    }

    fun countMandate(mandates: List<PaymentMandate>): Int {
        return mandates
            .filter { it.endDate == null }
            .count()
    }

    fun countTotalMandates(mandates: List<PaymentMandate>): Int {
        return mandates
            .filter { it.type == PaymentType.SEPA }
            .filter { it.endDate == null }
            .map { calculateAmount(it) }
            .sum()
            .toInt()
    }

    fun countTotalDonations(mandates: List<PaymentMandate>): Map<String, Double> {
        val date = LocalDate.now().minusYears(1)
        val query = "SELECT YEAR(t.created), MONTH(t.created), SUM(t.amount) FROM PaymentTransaction t WHERE t.status = :status GROUP BY YEAR(t.created), MONTH(t.created) ORDER BY YEAR(t.created), MONTH(t.created)"
        return entityManager
            .createQuery(query)
            .setParameter("status", PaymentTransactionStatus.SUCCESS)
            .resultList
            .map { it as Array<*> }
            .filter { !(it[0].toString().toInt() > date.year && it[1].toString().toInt() > date.month.value) }
            .map { (months[it[1].toString().toInt() - 1] + " " + it[0].toString()) to it[2] as Double }
            .toMap()
    }

    fun totalDonationsDestinationOnce(): Map<String, Double> {
        val query = "SELECT UPPER(d.destination), SUM(t.amount) FROM Donation d JOIN d.mandate.transactions t WHERE t.status = :status GROUP BY UPPER(d.destination)"
        return entityManager
            .createQuery(query)
            .setParameter("status", PaymentTransactionStatus.SUCCESS)
            .resultList
            .map { it as Array<*> }
            .map { (it[0]?.toString() ?: "UNKNOWN") to it[1] as Double }
            .toMap()
    }

    fun totalDonationsPerYear(): Map<String, Double> {
        val query = "SELECT YEAR(t.created), SUM(t.amount) FROM PaymentTransaction t WHERE t.status = :status GROUP BY YEAR(t.created)"
        return entityManager
            .createQuery(query)
            .setParameter("status", PaymentTransactionStatus.SUCCESS)
            .resultList
            .map { it as Array<*> }
            .map { it[0].toString() to it[1] as Double }
            .toMap()
    }

    private fun calculateAmount(mandate: PaymentMandate) = when (mandate.frequency) {
        PaymentFrequency.ONCE -> mandate.amount
        PaymentFrequency.MONTHLY -> mandate.amount * 12
        PaymentFrequency.QUARTERLY -> mandate.amount * 4
        PaymentFrequency.HALF_YEARLY -> mandate.amount * 2
        PaymentFrequency.YEARLY -> mandate.amount
    }

    fun PaymentMandate.isSuccess() = transactions.any { transaction -> transaction.isSuccess() }
    fun PaymentTransaction.isSuccess() = this.status == PaymentTransactionStatus.SUCCESS
    fun PaymentMandate.isOnce() = arrayOf(PaymentType.IDEAL, PaymentType.CREDIT_CARD).contains(type)
    fun PaymentMandate.isRecurring() = arrayOf(PaymentType.SEPA).contains(type)
}
