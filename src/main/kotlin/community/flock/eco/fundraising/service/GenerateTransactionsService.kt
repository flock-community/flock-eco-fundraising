package community.flock.eco.fundraising.service;

import community.flock.eco.feature.payment.model.PaymentFrequency
import community.flock.eco.feature.payment.model.PaymentMandate
import community.flock.eco.feature.payment.model.PaymentTransaction
import community.flock.eco.feature.payment.repositories.PaymentTransactionRepository
import community.flock.eco.fundraising.repositories.DonationRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.Month
import java.time.temporal.TemporalAdjusters.firstDayOfMonth
import java.time.temporal.TemporalAdjusters.lastDayOfMonth
import java.util.*

@Service
class GenerateTransactionsService(
        private val donationRepository: DonationRepository,
        private val paymentTransactionRepository: PaymentTransactionRepository) {

    @Value("\${flock.fundraising.donations.generate.dayOfMonth:1}")
    lateinit var dayOfMonth: Integer

    fun run() {
        val date = LocalDate.now()
        return this.run(date.year, date.month)
    }

    fun run(year: Int, month: Month) {

        val date = LocalDate.of(year, month, dayOfMonth.toInt())

        val mandatesWithTransactionForThisMonth = paymentTransactionRepository
                .findBetweenDate(date.with(firstDayOfMonth()), date.with(lastDayOfMonth()))
                .map { it.mandate }

        return donationRepository.findAll()
                .filter { donation ->
                    donation.mandate.startDate.isBefore(date)
                }
                .filter { donation ->
                    donation.mandate.endDate?.let { it.isAfter(date) } ?: true
                }
                .filter {
                    !mandatesWithTransactionForThisMonth.contains(it.mandate)
                }
                .filter { filterCollectionMonth(date, it.mandate) }
                .map { donation ->
                    PaymentTransaction(
                            amount = donation.mandate.amount,
                            mandate = donation.mandate,
                            reference = UUID.randomUUID().toString(),
                            created = date
                    )
                }
                .toList()
                .let {
                    paymentTransactionRepository.saveAll(it)
                }
    }

    fun filterCollectionMonth(date: LocalDate, mandate: PaymentMandate): Boolean {
        return when (mandate.frequency) {
            PaymentFrequency.ONCE -> false
            PaymentFrequency.MONTHLY -> true
            PaymentFrequency.QUARTERLY -> listOf(0, 3, 6, 9)
                    .map { date.plusMonths(it.toLong()).month }
                    .contains(mandate.collectionMonth)
            PaymentFrequency.HALF_YEARLY -> listOf(0, 6)
                    .map { date.plusMonths(it.toLong()).month }
                    .contains(mandate.collectionMonth)
            PaymentFrequency.YEARLY -> date.month == mandate.collectionMonth
        }
    }


}

