package community.flock.eco.fundraising.services

import community.flock.eco.feature.member.model.Member
import community.flock.eco.feature.member.model.MemberStatus
import community.flock.eco.feature.member.repositories.MemberRepository
import community.flock.eco.feature.member.specifications.MemberSpecification
import community.flock.eco.feature.payment.model.PaymentFrequency
import community.flock.eco.feature.payment.model.PaymentMandate
import community.flock.eco.feature.payment.model.PaymentTransaction
import community.flock.eco.feature.payment.model.PaymentTransactionStatus
import community.flock.eco.feature.payment.model.PaymentType
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

    fun sumTotalCollectionValue(mandates: List<PaymentMandate>): Double {
        return mandates
            .filter { it.type == PaymentType.SEPA }
            .filter { it.endDate == null }
            .map { calculateAmountPerYear(it.frequency, it.amount) }
            .sum()
    }

    fun sumTotalCollectionValueByDestination(mandates: List<PaymentMandate>): Map<String, Double> {
        data class Result(
            val destination: String?,
            val frequency: PaymentFrequency,
            val amount: Double
        )

        val query =
            "SELECT UPPER(d.destination), UPPER(d.mandate.frequency), SUM(d.mandate.amount) FROM Donation d WHERE d.mandate.endDate IS NULL AND d.mandate.type = :type GROUP BY UPPER(d.destination), UPPER(d.mandate.frequency)"
        val results = entityManager
            .createQuery(query)
            .setParameter("type", PaymentType.SEPA)
            .resultList
            .map { it as Array<*> }
            .map {
                Result(
                    destination = it[0] as String?,
                    frequency = it[1] as PaymentFrequency,
                    amount = it[2] as Double
                )
            }

        return results
            .groupingBy { it.destination ?: "UNKNOWN" }
            .fold(0.0) { acc, cur -> acc + calculateAmountPerYear(cur.frequency, cur.amount) }

    }

    fun countTotalDonations(mandates: List<PaymentMandate>): Map<String, Double> {
        val date = LocalDate.now().minusYears(1)
        val query =
            "SELECT YEAR(t.created), MONTH(t.created), SUM(t.amount) FROM PaymentTransaction t WHERE t.status = :status GROUP BY YEAR(t.created), MONTH(t.created) ORDER BY YEAR(t.created), MONTH(t.created)"
        return entityManager
            .createQuery(query)
            .setParameter("status", PaymentTransactionStatus.SUCCESS)
            .resultList
            .map { it as Array<*> }
            .filter { !(it[0].toString().toInt() > date.year && it[1].toString().toInt() > date.month.value) }
            .map { (months[it[1].toString().toInt() - 1] + " " + it[0].toString()) to it[2] as Double }
            .toMap()
    }

    fun totalDonationsByDestination(): Map<String, Double> {
        val query =
            "SELECT UPPER(d.destination), SUM(t.amount) FROM Donation d JOIN d.mandate.transactions t WHERE t.status = :status GROUP BY UPPER(d.destination)"
        return entityManager
            .createQuery(query)
            .setParameter("status", PaymentTransactionStatus.SUCCESS)
            .resultList
            .map { it as Array<*> }
            .map { (it[0]?.toString() ?: "UNKNOWN") to it[1] as Double }
            .toMap()
    }

    fun totalDonationsPerYear(): Map<String, Double> {
        val query =
            "SELECT YEAR(t.created), SUM(t.amount) FROM PaymentTransaction t WHERE t.status = :status GROUP BY YEAR(t.created)"
        return entityManager
            .createQuery(query)
            .setParameter("status", PaymentTransactionStatus.SUCCESS)
            .resultList
            .map { it as Array<*> }
            .map { it[0].toString() to it[1] as Double }
            .toMap()
    }

    private fun calculateAmountPerYear(frequency: PaymentFrequency, amount: Double) = when (frequency) {
        PaymentFrequency.ONCE -> amount
        PaymentFrequency.MONTHLY -> amount * 12
        PaymentFrequency.QUARTERLY -> amount * 4
        PaymentFrequency.HALF_YEARLY -> amount * 2
        PaymentFrequency.YEARLY -> amount
    }

    fun PaymentMandate.isSuccess() = transactions.any { transaction -> transaction.isSuccess() }
    fun PaymentTransaction.isSuccess() = this.status == PaymentTransactionStatus.SUCCESS
    fun PaymentMandate.isOnce() = arrayOf(PaymentType.IDEAL, PaymentType.CREDIT_CARD).contains(type)
    fun PaymentMandate.isRecurring() = arrayOf(PaymentType.SEPA).contains(type)
}
