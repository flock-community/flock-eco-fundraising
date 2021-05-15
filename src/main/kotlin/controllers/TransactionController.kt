package community.flock.eco.fundraising.controllers

import community.flock.eco.core.utils.toResponse
import community.flock.eco.feature.payment.model.PaymentMandate
import community.flock.eco.feature.payment.model.PaymentTransaction
import community.flock.eco.feature.payment.model.PaymentTransactionStatus
import community.flock.eco.feature.payment.model.PaymentType
import community.flock.eco.feature.payment.repositories.PaymentTransactionRepository
import community.flock.eco.fundraising.model.Donation
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import java.time.LocalDate

@RestController
@RequestMapping("/api/transactions")
class TransactionController(
    private val paymentTransactionRepository: PaymentTransactionRepository
) {

    data class ResponseMandateTransaction(val mandate: PaymentMandate, val transaction: PaymentTransaction)

    @GetMapping("/{year}/{month}")
    @PreAuthorize("hasAuthority('DonationsAuthority.READ')")
    fun findByMonthAndYear(
        @PathVariable("month") month: Int,
        @PathVariable("year") year: Int,
        page: Pageable
    ): ResponseEntity<List<ResponseMandateTransaction>> {

        val startDate = LocalDate.of(year, month, 1)
        val endDate = startDate.withDayOfMonth(startDate.lengthOfMonth())
        val type = PaymentType.SEPA
        return paymentTransactionRepository.findBetweenDate(startDate, endDate, type, page)
            .map { ResponseMandateTransaction(it.mandate, it) }
            .toResponse(page)
    }

    @PostMapping("/{year}/{month}/success")
    @PreAuthorize("hasAuthority('DonationsAuthority.WRITE')")
    fun markTransactionsSuccess(
        @PathVariable("month") month: Int,
        @PathVariable("year") year: Int
    ): ResponseEntity<List<Donation>> {

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

    @PostMapping("/{id}/status/{status}")
    @PreAuthorize("hasAuthority('DonationsAuthority.WRITE')")
    fun markTransactionStatus(
        @PathVariable("id") id: Long,
        @PathVariable("status") status: String
    ): ResponseEntity<Any> {

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
}
